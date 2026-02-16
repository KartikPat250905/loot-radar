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
        private const val USERS_COLLECTION = "users"
        private const val TOTAL_CLAIMED_VALUE_FIELD = "totalClaimedValue"
        private const val CLAIMED_GAME_IDS_FIELD = "claimedGameIds"
    }

    fun getClaimedValue(): Flow<Float> {
        return flowSettings.getFloatFlow(CLAIMED_VALUE_KEY, 0f)
    }

    fun getClaimedGameIds(): Flow<List<Long>> {
        return flowSettings.getStringFlow(CLAIMED_GAMES_KEY, "[]").map {
            Json.decodeFromString<List<Long>>(it)
        }
    }

    suspend fun syncClaimedValue() = withContext(Dispatchers.IO) {
        Logger.d("UserStatsRepository", "Starting to sync user stats.")
        try {
            val uid = authRepository.getAuthStateFlow().first()?.uid ?: run {
                Logger.d("UserStatsRepository", "User not logged in, aborting sync.")
                return@withContext
            }
            val docRef = firestore.collection(USERS_COLLECTION).document(uid)
            val snapshot = docRef.get().await()

            val localValue = settings.getFloat(CLAIMED_VALUE_KEY, 0f)
            val localGameIdsJson = settings.getString(CLAIMED_GAMES_KEY, "[]")
            val localGameIds = Json.decodeFromString<List<Long>>(localGameIdsJson)

            Logger.d("UserStatsRepository", "Local - Value: $localValue, GameIDs: ${localGameIds.size} items")

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

                Logger.d("UserStatsRepository", "Remote - Value: $remoteValue, GameIDs: ${remoteGameIds.size} items")

                val mergedGameIds = (localGameIds + remoteGameIds).distinct().sorted()
                val mergedValue = maxOf(localValue, remoteValue)

                Logger.d("UserStatsRepository", "Merged - Value: $mergedValue, GameIDs: ${mergedGameIds.size} items")

                settings[CLAIMED_VALUE_KEY] = mergedValue
                settings[CLAIMED_GAMES_KEY] = Json.encodeToString(mergedGameIds)

                docRef.set(
                    mapOf(
                        TOTAL_CLAIMED_VALUE_FIELD to mergedValue.toDouble(),
                        CLAIMED_GAME_IDS_FIELD to mergedGameIds
                    ),
                    SetOptions.merge()
                ).await()

                Logger.d("UserStatsRepository", "Successfully synced user stats.")
            } else {
                Logger.d("UserStatsRepository", "No user stats document found, creating one.")
                docRef.set(
                    mapOf(
                        TOTAL_CLAIMED_VALUE_FIELD to localValue.toDouble(),
                        CLAIMED_GAME_IDS_FIELD to localGameIds
                    )
                ).await()
                settings[CLAIMED_VALUE_KEY] = 0f
                settings[CLAIMED_GAMES_KEY] = "[]"
            }
        } catch (e: Exception) {
            Logger.e("UserStatsRepository", "Failed to sync user stats with Firestore: ", e)
        }
    }

    suspend fun addToClaimedValue(gameId: Long, worth: Float) = withContext(Dispatchers.IO) {
        val uid = authRepository.getAuthStateFlow().first()?.uid

        if (uid == null) {
            val localGameIdsJson = settings.getString(CLAIMED_GAMES_KEY, "[]")
            val localGameIds = Json.decodeFromString<List<Long>>(localGameIdsJson).toMutableList()

            if (!localGameIds.contains(gameId)) {
                localGameIds.add(gameId)
                val currentValue = settings.getFloat(CLAIMED_VALUE_KEY, 0f)
                val newValue = currentValue + worth

                settings[CLAIMED_VALUE_KEY] = newValue
                settings[CLAIMED_GAMES_KEY] = Json.encodeToString(localGameIds)

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
                        CLAIMED_GAME_IDS_FIELD to FieldValue.arrayUnion(gameId)
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
