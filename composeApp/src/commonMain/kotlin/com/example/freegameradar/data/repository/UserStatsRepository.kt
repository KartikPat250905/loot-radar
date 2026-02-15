package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.util.Logger
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getStringFlow
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

            if (snapshot.exists()) {
                val remoteValue = (snapshot.getDouble(TOTAL_CLAIMED_VALUE_FIELD) ?: 0.0).toFloat()
                val remoteGameIds = snapshot.get(CLAIMED_GAME_IDS_FIELD) as? List<Long> ?: emptyList()

                settings[CLAIMED_VALUE_KEY] = remoteValue
                settings[CLAIMED_GAMES_KEY] = Json.encodeToString(remoteGameIds)
                Logger.d("UserStatsRepository", "Successfully synced user stats from Firestore.")
            } else {
                Logger.d("UserStatsRepository", "No user stats document found, creating one.")
                docRef.set(
                    mapOf(
                        TOTAL_CLAIMED_VALUE_FIELD to 0.0,
                        CLAIMED_GAME_IDS_FIELD to emptyList<Long>()
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
        val uid = authRepository.getAuthStateFlow().first()?.uid ?: throw IllegalStateException("User not logged in.")
        val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val claimedGameIds = snapshot.get(CLAIMED_GAME_IDS_FIELD) as? List<Long> ?: emptyList()

                if (claimedGameIds.contains(gameId)) {
                    Logger.d("UserStatsRepository", "Game ID: $gameId already claimed, no action taken.")
                    return@runTransaction // Game already claimed, do nothing.
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

            // Manually update local cache after successful transaction
            syncClaimedValue()

        } catch (e: Exception) {
            Logger.e("UserStatsRepository", "Failed to add claimed value for game ID: $gameId: ", e)
            throw e
        }
    }
}
