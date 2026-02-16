package com.radarlabs.freegameradar.data.repository

import com.radarlabs.freegameradar.data.auth.AuthRepository
import com.radarlabs.freegameradar.util.Logger
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class UserStatsRepository(private val authRepository: AuthRepository, private val settings: ObservableSettings) {

    private val flowSettings: FlowSettings = settings.toFlowSettings()
    private val firestore = Firebase.firestore

    companion object {
        private const val CLAIMED_VALUE_KEY = "claimed_value"
        private const val CLAIMED_GAMES_KEY = "claimed_games"
        private const val CLAIMED_GAMES_WITH_TIMESTAMPS_KEY = "claimed_games_with_timestamps"
        private const val USERS_COLLECTION = "users"
        private const val TOTAL_CLAIMED_VALUE_FIELD = "totalClaimedValue"
        private const val CLAIMED_GAME_IDS_FIELD = "claimedGameIds"
        private const val CLAIMED_GAMES_WITH_TIMESTAMPS_FIELD = "claimedGamesWithTimestamps"
    }

    fun getClaimedValue(): Flow<Float> {
        return flowSettings.getFloatFlow(CLAIMED_VALUE_KEY, 0f)
    }

    fun getClaimedGameIds(): Flow<List<Long>> {
        return flowSettings.getStringFlow(CLAIMED_GAMES_WITH_TIMESTAMPS_KEY, "{}").map { json ->
            try {
                val map = Json.decodeFromString<Map<Long, Long>>(json)
                map.keys.toList().sorted()
            } catch (e: Exception) {
                try {
                    val oldJson = settings.getString(CLAIMED_GAMES_KEY, "[]")
                    Json.decodeFromString<List<Long>>(oldJson)
                } catch (e2: Exception) {
                    Logger.e("UserStatsRepository", "Failed to parse claimed games: ", e2)
                    emptyList()
                }
            }
        }
    }

    fun getClaimedGamesWithTimestamps(): Flow<Map<Long, Long>> {
        return flowSettings.getStringFlow(CLAIMED_GAMES_WITH_TIMESTAMPS_KEY, "{}").map { json ->
            try {
                Json.decodeFromString<Map<Long, Long>>(json)
            } catch (e: Exception) {
                Logger.e("UserStatsRepository", "Failed to parse claimed games with timestamps: ", e)
                emptyMap()
            }
        }
    }

    private suspend fun migrateOldDataIfNeeded() {
        try {
            val timestampsJson = settings.getString(CLAIMED_GAMES_WITH_TIMESTAMPS_KEY, "{}")
            val currentTimestamps = try {
                Json.decodeFromString<Map<Long, Long>>(timestampsJson)
            } catch (e: Exception) {
                emptyMap()
            }

            if (currentTimestamps.isNotEmpty()) {
                return
            }

            val oldJson = settings.getString(CLAIMED_GAMES_KEY, "[]")
            val oldGameIds = try {
                Json.decodeFromString<List<Long>>(oldJson)
            } catch (e: Exception) {
                try {
                    val map = Json.decodeFromString<Map<Long, Long>>(oldJson)
                    settings[CLAIMED_GAMES_WITH_TIMESTAMPS_KEY] = oldJson
                    Logger.d("UserStatsRepository", "Found timestamps in old key, moved to new key")
                    return
                } catch (e2: Exception) {
                    emptyList()
                }
            }

            if (oldGameIds.isNotEmpty()) {
                val migratedMap = oldGameIds.associateWith { 0L }
                settings[CLAIMED_GAMES_WITH_TIMESTAMPS_KEY] = Json.encodeToString(migratedMap)
                Logger.d("UserStatsRepository", "Migrated ${oldGameIds.size} games from old format")
            }
        } catch (e: Exception) {
            Logger.e("UserStatsRepository", "Error during migration: ", e)
        }
    }

    suspend fun syncClaimedValue() = withContext(Dispatchers.IO) {
        Logger.d("UserStatsRepository", "Starting to sync user stats.")

        migrateOldDataIfNeeded()

        try {
            val uid = authRepository.getAuthStateFlow().first()?.uid ?: run {
                Logger.d("UserStatsRepository", "User not logged in, aborting sync.")
                return@withContext
            }
            val docRef = firestore.collection(USERS_COLLECTION).document(uid)
            val snapshot = docRef.get().await()

            val localValue = settings.getFloat(CLAIMED_VALUE_KEY, 0f)
            val localGamesWithTimestampsJson = settings.getString(CLAIMED_GAMES_WITH_TIMESTAMPS_KEY, "{}")
            val localGamesWithTimestamps = try {
                Json.decodeFromString<Map<Long, Long>>(localGamesWithTimestampsJson)
            } catch (e: Exception) {
                Logger.e("UserStatsRepository", "Failed to parse local timestamps: ", e)
                emptyMap()
            }

            Logger.d("UserStatsRepository", "Local - Value: $localValue, Games: ${localGamesWithTimestamps.size} items")

            if (snapshot.exists()) {
                val remoteValue = (snapshot.getDouble(TOTAL_CLAIMED_VALUE_FIELD) ?: 0.0).toFloat()
                val remoteGameIds = (snapshot.get(CLAIMED_GAME_IDS_FIELD) as? List<*>)
                    ?.mapNotNull {
                        when (it) {
                            is Long -> it
                            is Number -> it.toLong()
                            else -> null
                        }
                    } ?: emptyList()

                val remoteGamesWithTimestamps = (snapshot.get(CLAIMED_GAMES_WITH_TIMESTAMPS_FIELD) as? Map<*, *>)
                    ?.mapNotNull { (key, value) ->
                        val gameId = when (key) {
                            is String -> key.toLongOrNull()
                            is Long -> key
                            is Number -> key.toLong()
                            else -> null
                        }
                        val timestamp = when (value) {
                            is Long -> value
                            is Number -> value.toLong()
                            else -> null
                        }
                        if (gameId != null && timestamp != null) gameId to timestamp else null
                    }?.toMap() ?: emptyMap()

                Logger.d("UserStatsRepository", "Remote - Value: $remoteValue, Games: ${remoteGamesWithTimestamps.size} items")

                val mergedGamesWithTimestamps = (localGamesWithTimestamps + remoteGamesWithTimestamps).toMutableMap()

                remoteGameIds.forEach { gameId ->
                    if (!mergedGamesWithTimestamps.containsKey(gameId)) {
                        mergedGamesWithTimestamps[gameId] = 0L
                    }
                }

                val mergedValue = maxOf(localValue, remoteValue)

                Logger.d("UserStatsRepository", "Merged - Value: $mergedValue, Games: ${mergedGamesWithTimestamps.size} items")

                settings[CLAIMED_VALUE_KEY] = mergedValue
                settings[CLAIMED_GAMES_WITH_TIMESTAMPS_KEY] = Json.encodeToString(mergedGamesWithTimestamps)

                val mergedGameIdsList = mergedGamesWithTimestamps.keys.toList().sorted()

                docRef.set(
                    mapOf(
                        TOTAL_CLAIMED_VALUE_FIELD to mergedValue.toDouble(),
                        CLAIMED_GAME_IDS_FIELD to mergedGameIdsList,
                        CLAIMED_GAMES_WITH_TIMESTAMPS_FIELD to mergedGamesWithTimestamps.mapKeys { it.key.toString() }
                    ),
                    SetOptions.merge()
                ).await()

                Logger.d("UserStatsRepository", "Successfully synced user stats.")
            } else {
                Logger.d("UserStatsRepository", "No user stats document found, creating one.")
                val gameIdsList = localGamesWithTimestamps.keys.toList().sorted()
                docRef.set(
                    mapOf(
                        TOTAL_CLAIMED_VALUE_FIELD to localValue.toDouble(),
                        CLAIMED_GAME_IDS_FIELD to gameIdsList,
                        CLAIMED_GAMES_WITH_TIMESTAMPS_FIELD to localGamesWithTimestamps.mapKeys { it.key.toString() }
                    )
                ).await()
            }
        } catch (e: Exception) {
            Logger.e("UserStatsRepository", "Failed to sync user stats with Firestore: ", e)
        }
    }

    suspend fun addToClaimedValue(gameId: Long, worth: Float) = withContext(Dispatchers.IO) {
        val uid = authRepository.getAuthStateFlow().first()?.uid
        val currentTimestamp = System.currentTimeMillis()

        if (uid == null) {
            val localGamesWithTimestampsJson = settings.getString(CLAIMED_GAMES_WITH_TIMESTAMPS_KEY, "{}")
            val localGamesWithTimestamps = try {
                Json.decodeFromString<Map<Long, Long>>(localGamesWithTimestampsJson).toMutableMap()
            } catch (e: Exception) {
                Logger.e("UserStatsRepository", "Failed to parse local timestamps: ", e)
                mutableMapOf()
            }

            if (!localGamesWithTimestamps.containsKey(gameId)) {
                localGamesWithTimestamps[gameId] = currentTimestamp
                val currentValue = settings.getFloat(CLAIMED_VALUE_KEY, 0f)
                val newValue = currentValue + worth

                settings[CLAIMED_VALUE_KEY] = newValue
                settings[CLAIMED_GAMES_WITH_TIMESTAMPS_KEY] = Json.encodeToString(localGamesWithTimestamps)

                Logger.d("UserStatsRepository", "Saved claimed game locally. Game ID: $gameId, New total: $newValue")
            }
            return@withContext
        }

        val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val claimedGameIds = (snapshot.get(CLAIMED_GAME_IDS_FIELD) as? List<*>)
                    ?.mapNotNull {
                        when (it) {
                            is Long -> it
                            is Number -> it.toLong()
                            else -> null
                        }
                    } ?: emptyList()

                if (claimedGameIds.contains(gameId)) {
                    Logger.d("UserStatsRepository", "Game ID: $gameId already claimed, no action taken.")
                    return@runTransaction
                }

                val currentTotal = snapshot.getDouble(TOTAL_CLAIMED_VALUE_FIELD) ?: 0.0
                val newCalculatedTotal = currentTotal + worth.toDouble()

                transaction.update(
                    userDocRef, mapOf(
                        TOTAL_CLAIMED_VALUE_FIELD to newCalculatedTotal,
                        CLAIMED_GAME_IDS_FIELD to FieldValue.arrayUnion(gameId),
                        "$CLAIMED_GAMES_WITH_TIMESTAMPS_FIELD.$gameId" to currentTimestamp
                    )
                )
                Logger.d("UserStatsRepository", "Updated claimed value for game ID: $gameId. New total: $newCalculatedTotal")

            }.await()

            syncClaimedValue()

        } catch (e: Exception) {
            Logger.e("UserStatsRepository", "Failed to add claimed value for game ID: $gameId: ", e)
            throw e
        }
    }
}
