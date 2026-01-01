package com.example.freegameradar.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.db.User_settings
import com.example.freegameradar.settings.UserSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository
) : UserSettingsRepository {

    private val db = GameDatabaseProvider.getDatabase().user_settingsQueries

    override fun getSettings(): Flow<UserSettings> = flow {
        val userId = authRepository.getAuthStateFlow().first()?.uid

        if (userId == null) {
            emit(UserSettings()) // User is not authenticated, emit default and end flow.
            return@flow
        }

        try {
            val remoteDb = Firebase.firestore
            val userDocRef = remoteDb.collection("users").document(userId)

            // Safely update notification token for any authenticated user.
            val token = Firebase.messaging.token.await()
            if (token.isNotBlank()) {
                // Use set with merge to create the document if it doesn't exist.
                userDocRef.set(mapOf("notificationTokens" to FieldValue.arrayUnion(token)), SetOptions.merge()).await()
            }

            val settings = userDocRef.get().await().toObject(UserSettings::class.java)

            if (settings != null) {
                // Sync remote settings to local db for offline access
                db.insertSettings(
                    notifications_enabled = if (settings.notificationsEnabled) 1L else 0L,
                    preferred_game_platforms = settings.preferredGamePlatforms.joinToString(","),
                    preferred_game_types = settings.preferredGameTypes.joinToString(",")
                )
                emit(settings)
            } else {
                emit(UserSettings()) // Emit default if no settings exist on Firebase.
            }
        } catch (e: Exception) {
            // Fallback to local DB if Firebase fails (e.g., offline)
            println("Failed to fetch remote settings, falling back to local. Reason: ${e.message}")
            val localSettings = db.getSettings().asFlow().mapToOneOrDefault(User_settings(0, 1L, "", ""), Dispatchers.IO).first()
            emit(UserSettings(
                notificationsEnabled = localSettings.notifications_enabled == 1L,
                preferredGamePlatforms = localSettings.preferred_game_platforms.split(",").filter { it.isNotEmpty() },
                preferredGameTypes = localSettings.preferred_game_types.split(",").filter { it.isNotEmpty() },
                notificationTokens = emptyList() // Tokens are not stored locally.
            ))
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun saveSettings(userSettings: UserSettings) {
        withContext(Dispatchers.IO) {
            // Save to local DB for offline access.
            db.insertSettings(
                notifications_enabled = if (userSettings.notificationsEnabled) 1L else 0L,
                preferred_game_platforms = userSettings.preferredGamePlatforms.joinToString(","),
                preferred_game_types = userSettings.preferredGameTypes.joinToString(",")
            )

            // Save to remote for any authenticated user (guest or registered).
            val userId = authRepository.getAuthStateFlow().first()?.uid ?: return@withContext

            try {
                val remoteDb = Firebase.firestore
                // Use SetOptions.merge() to avoid overwriting other user data like claimed games.
                remoteDb.collection("users").document(userId).set(userSettings, SetOptions.merge()).await()
            } catch (e: Exception) {
                // Ignore Firebase error if offline, as the local copy is already saved.
                println("Failed to save settings to Firebase: ${e.message}")
            }
        }
    }
}
