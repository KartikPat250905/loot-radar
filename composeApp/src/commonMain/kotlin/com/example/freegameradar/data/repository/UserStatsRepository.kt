package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.set
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserStatsRepository(private val authRepository: AuthRepository, private val settings: ObservableSettings) {

    private val flowSettings: FlowSettings = settings.toFlowSettings()
    private val firestore = Firebase.firestore

    companion object {
        private const val CLAIMED_VALUE_KEY = "claimed_value"
        private const val USERS_COLLECTION = "users"
        private const val TOTAL_CLAIMED_VALUE_FIELD = "totalClaimedValue"
    }

    fun getClaimedValue(): Flow<Float> {
        // The UI reads from the local cache for speed and offline availability.
        return flowSettings.getFloatFlow(CLAIMED_VALUE_KEY, 0f)
    }

    suspend fun syncClaimedValue() = withContext(Dispatchers.IO) {
        try {
            val uid = authRepository.getAuthStateFlow().first()?.uid ?: return@withContext
            val docRef = firestore.collection(USERS_COLLECTION).document(uid)
            val snapshot = docRef.get().await()

            val remoteValue = if (snapshot.exists()) {
                (snapshot.getDouble(TOTAL_CLAIMED_VALUE_FIELD) ?: 0.0).toFloat()
            } else {
                // If document doesn't exist, create it for the new user.
                docRef.set(mapOf(TOTAL_CLAIMED_VALUE_FIELD to 0.0)).await()
                0f
            }
            // Update local cache with the source of truth from Firebase.
            settings[CLAIMED_VALUE_KEY] = remoteValue
        } catch (e: Exception) {
            // Log the exception, but don't crash. The app will just use the stale local data.
            println("Failed to sync claimed value with Firestore: ${e.message}")
        }
    }

    suspend fun addToClaimedValue(worth: Float) = withContext(Dispatchers.IO) {
        val uid = authRepository.getAuthStateFlow().first()?.uid ?: throw IllegalStateException("User not logged in.")
        val userDocRef = firestore.collection(USERS_COLLECTION).document(uid)

        try {
            // Run a transaction to ensure atomic update on the server.
            val newTotal = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDocRef)
                val currentTotal = snapshot.getDouble(TOTAL_CLAIMED_VALUE_FIELD) ?: 0.0
                val newCalculatedTotal = currentTotal + worth.toDouble()
                transaction.set(userDocRef, mapOf(TOTAL_CLAIMED_VALUE_FIELD to newCalculatedTotal), SetOptions.merge())
                newCalculatedTotal.toFloat()
            }.await()

            // Once Firestore is successfully updated, update our local cache.
            settings[CLAIMED_VALUE_KEY] = newTotal

        } catch (e: Exception) {
            // If the transaction fails, the local cache is not updated, maintaining consistency.
            println("Failed to add claimed value: ${e.message}")
            throw e // Propagate the error to the ViewModel/UI to give user feedback if needed.
        }
    }
}
