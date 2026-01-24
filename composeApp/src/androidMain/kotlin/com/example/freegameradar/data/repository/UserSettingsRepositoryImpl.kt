package com.example.freegameradar.data.repository

import android.util.Log
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
            // Unauthenticated: Provide default settings from local cache
            db.getSettings().asFlow().mapToOneOrDefault(
                User_settings(0, 0L, "", ""), 
                Dispatchers.IO
            ).map { local ->
                UserSettings(
                    notificationsEnabled = local.notifications_enabled == 1L,
                    preferredGamePlatforms = local.preferred_game_platforms.split(',').filter { it.isNotEmpty() },
                    preferredGameTypes = local.preferred_game_types.split(',').filter { it.isNotEmpty() }
                )
            }
        } else {
            // Authenticated (including anonymous): Source of truth is Firestore
            callbackFlow<UserSettings> {
                val docRef = Firebase.firestore.collection("users").document(user.uid)

                val subscription = docRef.addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Firestore listen failed: $error")
                        close(error)
                        return@addSnapshotListener
                    }

                    val emptySettings = UserSettings(
                        notificationsEnabled = false,
                        preferredGamePlatforms = emptyList(),
                        preferredGameTypes = emptyList()
                    )

                    val remoteSettings = if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        if (data != null && data.containsKey("setupComplete")) {
                            // Full user settings exist
                            snapshot.toObject<UserSettings>() ?: emptySettings
                        } else {
                            // Document exists but only has FCM token - return defaults
                            emptySettings
                        }
                    } else {
                        // New user - no document yet
                        emptySettings
                    }

                    // Sync to local cache
                    launch(Dispatchers.IO) {
                        db.insertSettings(
                            notifications_enabled = if (remoteSettings.notificationsEnabled) 1L else 0L,
                            preferred_game_platforms = remoteSettings.preferredGamePlatforms.joinToString(","),
                            preferred_game_types = remoteSettings.preferredGameTypes.joinToString(",")
                        )
                    }

                    trySend(remoteSettings)
                }

                awaitClose { subscription.remove() }
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncUserSettings() {
        // Redundant - handled by reactive listener
    }

    override suspend fun saveSettings(userSettings: UserSettings) {
        val userId = authRepository.getAuthStateFlow().first()?.uid ?: return
        withContext(Dispatchers.IO) {
            try {
                val settingsData = mapOf(
                    "notificationsEnabled" to userSettings.notificationsEnabled,
                    "preferredGamePlatforms" to userSettings.preferredGamePlatforms,
                    "preferredGameTypes" to userSettings.preferredGameTypes,
                    "setupComplete" to userSettings.setupComplete
                )
                Firebase.firestore.collection("users").document(userId)
                    .set(settingsData, SetOptions.merge()).await()
                    
                Log.d("UserSettingsRepo", "✅ Settings saved for user: $userId")
            } catch (e: Exception) {
                Log.e("UserSettingsRepo", "❌ Failed to save settings: ${e.message}", e)
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
