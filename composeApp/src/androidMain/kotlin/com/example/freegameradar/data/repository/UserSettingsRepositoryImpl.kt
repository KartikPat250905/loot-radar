package com.example.freegameradar.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.db.User_settings
import com.example.freegameradar.settings.UserSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository
) : UserSettingsRepository {

    private val db = GameDatabaseProvider.getDatabase().user_settingsQueries

    // 1. Rewritten getSettings to be CLOUD-FIRST.
    actual override fun getSettings(): Flow<UserSettings> = flow {
        val userId = authRepository.getAuthStateFlow().first()?.uid
        if (userId == null) {
            emit(UserSettings()) // No user logged in, emit default settings and end the flow.
            return@flow
        }

        try {
            // ALWAYS prioritize fetching from Firestore. This is the source of truth.
            val remoteDb = Firebase.firestore
            val userDocRef = remoteDb.collection("users").document(userId)
            val remoteSettings = userDocRef.get().await().toObject(UserSettings::class.java)

            if (remoteSettings != null) {
                // Cloud data exists. This is our source of truth.
                // Cache it locally for offline use.
                db.insertSettings(
                    notifications_enabled = if (remoteSettings.notificationsEnabled) 1L else 0L,
                    preferred_game_platforms = remoteSettings.preferredGamePlatforms.joinToString(","),
                    preferred_game_types = remoteSettings.preferredGameTypes.joinToString(",")
                )
                // Emit the fresh cloud data.
                emit(remoteSettings)
            } else {
                // No settings in the cloud (e.g., a new user). Emit default settings.
                emit(UserSettings())
            }
        } catch (e: Exception) {
            // Firestore failed (e.g., offline). Fallback to the local cache.
            println("Cloud-First: Failed to fetch remote settings, falling back to local cache. Reason: ${e.message}")
            val localSettings = db.getSettings().asFlow().mapToOneOrDefault(User_settings(0, 1L, "", ""), Dispatchers.IO).first()
            emit(UserSettings(
                notificationsEnabled = localSettings.notifications_enabled == 1L,
                preferredGamePlatforms = localSettings.preferred_game_platforms.split(",").filter { it.isNotEmpty() },
                preferredGameTypes = localSettings.preferred_game_types.split(",").filter { it.isNotEmpty() }
            ))
        }
    }.flowOn(Dispatchers.IO)

    // This function's logic is now integrated into the new getSettings() flow.
    // It's kept to satisfy the interface and avoid breaking App.kt, but can be removed in a future refactor.
    actual override suspend fun syncUserSettings() {
       // Intentionally left blank.
    }

    // 3. saveSettings remains correct: it saves locally and then pushes to remote.
    actual override suspend fun saveSettings(userSettings: UserSettings) {
        withContext(Dispatchers.IO) {
            // Update local DB first.
            db.insertSettings(
                notifications_enabled = if (userSettings.notificationsEnabled) 1L else 0L,
                preferred_game_platforms = userSettings.preferredGamePlatforms.joinToString(","),
                preferred_game_types = userSettings.preferredGameTypes.joinToString(",")
            )

            // Then, push changes to remote for any authenticated user.
            val userId = authRepository.getAuthStateFlow().first()?.uid ?: return@withContext
            try {
                val remoteDb = Firebase.firestore
                remoteDb.collection("users").document(userId).set(userSettings, SetOptions.merge()).await()
            } catch (e: Exception) {
                println("Failed to save settings to Firebase: ${e.message}")
            }
        }
    }
}
