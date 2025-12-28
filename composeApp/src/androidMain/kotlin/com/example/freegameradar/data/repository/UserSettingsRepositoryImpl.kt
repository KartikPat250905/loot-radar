package com.example.freegameradar.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.db.User_settings
import com.example.freegameradar.settings.UserSettings
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
        if (authRepository.isUserLoggedIn()) {
            try {
                val userId = Firebase.auth.currentUser?.uid ?: run {
                    emit(UserSettings()) // Emit default and end flow if no user ID
                    return@flow
                }
                val remoteDb = Firebase.firestore
                val settings = remoteDb.collection("users").document(userId).get().await()
                    .toObject(UserSettings::class.java)

                if (settings != null) {
                    // Sync remote settings with local db for offline access
                    db.insertSettings(
                        notifications_enabled = if (settings.notificationsEnabled) 1L else 0L,
                        preferred_game_platforms = settings.preferredGamePlatforms.joinToString(","),
                        preferred_game_types = settings.preferredGameTypes.joinToString(",")
                    )
                    emit(settings)
                } else {
                    emit(UserSettings()) // Emit default if no settings on Firebase
                }
            } catch (e: Exception) {
                // Fallback to local DB if Firebase fails (e.g., offline)
                println("Failed to fetch remote settings, falling back to local. Reason: ${e.message}")
                val localSettings = db.getSettings().asFlow().mapToOneOrDefault(User_settings(0, 1L, "", ""), Dispatchers.IO).first()
                emit(UserSettings(
                    notificationsEnabled = localSettings.notifications_enabled == 1L,
                    preferredGamePlatforms = localSettings.preferred_game_platforms.split(",").filter { it.isNotEmpty() },
                    preferredGameTypes = localSettings.preferred_game_types.split(",").filter { it.isNotEmpty() }
                ))
            }
        } else {
            // Guest user or not logged in: use local DB
            val localSettings = db.getSettings().asFlow().mapToOneOrDefault(User_settings(0, 1L, "", ""), Dispatchers.IO).first()
            emit(UserSettings(
                notificationsEnabled = localSettings.notifications_enabled == 1L,
                preferredGamePlatforms = localSettings.preferred_game_platforms.split(",").filter { it.isNotEmpty() },
                preferredGameTypes = localSettings.preferred_game_types.split(",").filter { it.isNotEmpty() }
            ))
        }
    }.flowOn(Dispatchers.IO) // Ensure all logic in the flow runs on a background thread

    override suspend fun saveSettings(userSettings: UserSettings) {
        withContext(Dispatchers.IO) { // Ensure all logic runs on a background thread
            // Always save to local DB first
            db.insertSettings(
                notifications_enabled = if (userSettings.notificationsEnabled) 1L else 0L,
                preferred_game_platforms = userSettings.preferredGamePlatforms.joinToString(","),
                preferred_game_types = userSettings.preferredGameTypes.joinToString(",")
            )

            if (authRepository.isUserLoggedIn()) {
                try {
                    val userId = Firebase.auth.currentUser?.uid ?: return@withContext
                    val remoteDb = Firebase.firestore
                    remoteDb.collection("users").document(userId).set(userSettings).await()
                } catch (e: Exception) {
                    // Ignore Firebase error if offline, local copy is already saved.
                    println("Failed to save settings to Firebase: ${e.message}")
                }
            }
        }
    }
}
