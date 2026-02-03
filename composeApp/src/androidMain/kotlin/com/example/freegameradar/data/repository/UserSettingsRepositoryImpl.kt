package com.example.freegameradar.data.repository

import android.content.Context
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository,
    context: Any // Match the expect declaration
) : UserSettingsRepository {

    private val androidContext = context as Context // Cast to Android Context
    private val db = GameDatabaseProvider.getDatabase().user_settingsQueries
    private val prefs = androidContext.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    override fun getSettings(): Flow<UserSettings> = authRepository.getAuthStateFlow().flatMapLatest { user ->
        // For both authenticated and unauthenticated users, the local SQLDelight cache is the source of truth.
        // `syncUserSettings` is responsible for populating this cache from Firestore for authenticated users.
        db.getSettings().asFlow().mapToOneOrDefault(
            // Default settings if DB is empty
            User_settings(0, 0L, "", ""),
            Dispatchers.IO
        ).map { local ->
            // `setupComplete` is not stored in SQLDelight, so we get it from SharedPreferences.
            val setupComplete = if (user != null) prefs.getBoolean("setup_complete", false) else false
            UserSettings(
                notificationsEnabled = local.notifications_enabled == 1L,
                preferredGamePlatforms = local.preferred_game_platforms.split(',').filter { it.isNotEmpty() },
                preferredGameTypes = local.preferred_game_types.split(',').filter { it.isNotEmpty() },
                setupComplete = setupComplete
            )
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun syncUserSettings() {
        val user = authRepository.getAuthStateFlow().first()
        if (user == null) {
            Log.d("UserSettingsRepo", "Sync skipped: User not authenticated.")
            return
        }

        withContext(Dispatchers.IO) {
            try {
                val docRef = Firebase.firestore.collection("users").document(user.uid)
                val snapshot = docRef.get().await()

                val emptySettings = UserSettings(setupComplete = false)

                val remoteSettings = if (snapshot != null && snapshot.exists()) {
                    snapshot.toObject<UserSettings>() ?: emptySettings
                } else {
                    Log.d("UserSettingsRepo", "No remote settings found for user ${user.uid}, using defaults.")
                    emptySettings
                }

                // Sync remote settings to local caches
                db.insertSettings(
                    notifications_enabled = if (remoteSettings.notificationsEnabled) 1L else 0L,
                    preferred_game_platforms = remoteSettings.preferredGamePlatforms.joinToString(","),
                    preferred_game_types = remoteSettings.preferredGameTypes.joinToString(",")
                )

                // Persist setupComplete flag separately in SharedPreferences
                prefs.edit().putBoolean("setup_complete", remoteSettings.setupComplete).apply()

                Log.d("UserSettingsRepo", "✅ Settings synced for user: ${user.uid}")

            } catch (e: Exception) {
                Log.e("UserSettingsRepo", "❌ Failed to sync settings: ${e.message}", e)
            }
        }
    }

    override suspend fun saveSettings(userSettings: UserSettings) {
        val userId = authRepository.getAuthStateFlow().first()?.uid ?: return
        withContext(Dispatchers.IO) {
            try {
                // Persist setupComplete flag before writing to Firestore
                prefs.edit().putBoolean("setup_complete", userSettings.setupComplete).apply()

                // Also update local DB immediately for a responsive UI
                db.insertSettings(
                    notifications_enabled = if (userSettings.notificationsEnabled) 1L else 0L,
                    preferred_game_platforms = userSettings.preferredGamePlatforms.joinToString(","),
                    preferred_game_types = userSettings.preferredGameTypes.joinToString(",")
                )

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
        val currentSettings = getSettings().first()
        // Preserve setupComplete status, only disable notifications
        val disabledSettings = currentSettings.copy(notificationsEnabled = false)
        saveSettings(disabledSettings)
    }
}
