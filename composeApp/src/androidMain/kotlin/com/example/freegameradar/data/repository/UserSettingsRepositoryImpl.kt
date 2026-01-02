package com.example.freegameradar.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.db.User_settings
import com.example.freegameradar.settings.UserSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository
) : UserSettingsRepository {

    private val db = GameDatabaseProvider.getDatabase().user_settingsQueries

    override fun getSettings(): Flow<UserSettings> = authRepository.getAuthStateFlow().flatMapLatest { user ->
        if (user == null) {
            // Unauthenticated: Provide settings from the local cache.
            db.getSettings().asFlow().mapToOneOrDefault(User_settings(0, 1L, "", ""), Dispatchers.IO).map { local ->
                UserSettings(
                    notificationsEnabled = local.notifications_enabled == 1L,
                    preferredGamePlatforms = local.preferred_game_platforms.split(',').filter { it.isNotEmpty() },
                    preferredGameTypes = local.preferred_game_types.split(',').filter { it.isNotEmpty() }
                )
            }
        } else {
            // Authenticated: The source of truth is Firestore. We listen to it for real-time updates.
            callbackFlow<UserSettings> {
                val docRef = Firebase.firestore.collection("users").document(user.uid)

                val subscription = docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Firestore listen failed: $error")
                        close(error)
                        return@addSnapshotListener
                    }

                    val remoteSettings = if (snapshot != null && snapshot.exists()) {
                        snapshot.toObject<UserSettings>() ?: UserSettings()
                    } else {
                        UserSettings() // For a new user or if the document doesn't exist.
                    }

                    // Sync the latest settings from Firestore to our local cache.
                    launch(Dispatchers.IO) {
                        db.insertSettings(
                            notifications_enabled = if (remoteSettings.notificationsEnabled) 1L else 0L,
                            preferred_game_platforms = remoteSettings.preferredGamePlatforms.joinToString(","),
                            preferred_game_types = remoteSettings.preferredGameTypes.joinToString(",")
                        )
                    }

                    // Emit the latest settings to the UI.
                    trySend(remoteSettings)
                }

                // Remove the listener when the flow is cancelled.
                awaitClose { subscription.remove() }
            }
        }
    }.flowOn(Dispatchers.IO)


    override suspend fun syncUserSettings() {
        // This function is redundant due to the new reactive implementation of getSettings.
    }

    override suspend fun saveSettings(userSettings: UserSettings) {
        val userId = authRepository.getAuthStateFlow().first()?.uid ?: return
        withContext(Dispatchers.IO) {
            try {
                // The source of truth is Firestore. We only need to save it here.
                // The listener in getSettings() will handle updating the cache and UI.
                Firebase.firestore.collection("users").document(userId).set(userSettings, SetOptions.merge()).await()
            } catch (e: Exception) {
                println("Failed to save settings to Firebase: ${e.message}")
            }
        }
    }

    override suspend fun disableAllNotifications() {
        val disabledSettings = UserSettings(
            notificationsEnabled = false,
            preferredGamePlatforms = emptyList(),
            preferredGameTypes = emptyList()
        )
        saveSettings(disabledSettings)
    }
}