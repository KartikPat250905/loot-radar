package com.radarlabs.freegameradar.data.repository

import android.content.Context
import android.util.Log
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.radarlabs.freegameradar.data.GameDatabaseProvider
import com.radarlabs.freegameradar.data.auth.AuthRepository
import com.radarlabs.freegameradar.db.User_settings
import com.radarlabs.freegameradar.settings.UserSettings
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository,
    context: Any
) : UserSettingsRepository {

    private val androidContext = context as Context
    private val db = GameDatabaseProvider.getDatabase().user_settingsQueries
    private val prefs = androidContext.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    // ✅ Track initial sync completion
    private val _hasCompletedInitialSync = MutableStateFlow(false)
    override val hasCompletedInitialSync: StateFlow<Boolean> = _hasCompletedInitialSync

    override fun getSettings(): Flow<UserSettings> = authRepository.getAuthStateFlow().flatMapLatest { user ->
        db.getSettings().asFlow().mapToOneOrDefault(
            User_settings(0, 0L, "", ""),
            Dispatchers.IO
        ).map { local ->
            val setupComplete = if (user != null) {
                prefs.getBoolean("setup_complete", false)
            } else {
                false
            }
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
            _hasCompletedInitialSync.value = true
            return
        }

        withContext(Dispatchers.IO) {
            try {
                Log.d("UserSettingsRepo", "🔄 Starting sync for user: ${user.uid}")
                val docRef = Firebase.firestore.collection("users").document(user.uid)
                val snapshot = docRef.get().await()

                Log.d("UserSettingsRepo", "📄 Document exists: ${snapshot.exists()}")
                Log.d("UserSettingsRepo", "📄 Raw data: ${snapshot.data}")

                val emptySettings = UserSettings(setupComplete = false)

                val remoteSettings = if (snapshot != null && snapshot.exists()) {
                    val data = snapshot.data
                    if (data != null) {
                        // ✅ Log each field individually
                        val setupComplete = data["setupComplete"]
                        val notificationsEnabled = data["notificationsEnabled"]
                        val platformsRaw = data["preferredGamePlatforms"]
                        val typesRaw = data["preferredGameTypes"]

                        Log.d("UserSettingsRepo", "🔍 setupComplete RAW: $setupComplete (type: ${setupComplete?.javaClass?.simpleName})")
                        Log.d("UserSettingsRepo", "🔍 notificationsEnabled RAW: $notificationsEnabled (type: ${notificationsEnabled?.javaClass?.simpleName})")
                        Log.d("UserSettingsRepo", "🔍 platforms RAW: $platformsRaw (type: ${platformsRaw?.javaClass?.simpleName})")
                        Log.d("UserSettingsRepo", "🔍 types RAW: $typesRaw (type: ${typesRaw?.javaClass?.simpleName})")

                        val setupCompleteBool = setupComplete as? Boolean ?: false
                        val notificationsEnabledBool = notificationsEnabled as? Boolean ?: false
                        val platforms = (platformsRaw as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
                        val types = (typesRaw as? List<*>)?.mapNotNull { it as? String } ?: emptyList()

                        Log.d("UserSettingsRepo", "📦 Parsed data: setupComplete=$setupCompleteBool, notif=$notificationsEnabledBool, platforms=$platforms, types=$types")

                        UserSettings(
                            setupComplete = setupCompleteBool,
                            notificationsEnabled = notificationsEnabledBool,
                            preferredGamePlatforms = platforms,
                            preferredGameTypes = types
                        )
                    } else {
                        Log.d("UserSettingsRepo", "Document exists but data is null")
                        emptySettings
                    }
                } else {
                    Log.d("UserSettingsRepo", "No remote settings found for user ${user.uid}")
                    emptySettings
                }

                Log.d("UserSettingsRepo", "💾 Saving to local: setupComplete=${remoteSettings.setupComplete}, notif=${remoteSettings.notificationsEnabled}")

                // Save to local caches
                db.insertSettings(
                    notifications_enabled = if (remoteSettings.notificationsEnabled) 1L else 0L,
                    preferred_game_platforms = remoteSettings.preferredGamePlatforms.joinToString(","),
                    preferred_game_types = remoteSettings.preferredGameTypes.joinToString(",")
                )

                prefs.edit().putBoolean("setup_complete", remoteSettings.setupComplete).apply()

                Log.d("UserSettingsRepo", "✅ Settings synced - setupComplete=${remoteSettings.setupComplete}")

            } catch (e: Exception) {
                Log.e("UserSettingsRepo", "❌ Failed to sync settings: ${e.message}", e)
                e.printStackTrace()
            } finally {
                _hasCompletedInitialSync.value = true
            }
        }
    }


    override suspend fun saveSettings(userSettings: UserSettings) {
        val userId = authRepository.getAuthStateFlow().first()?.uid ?: return
        withContext(Dispatchers.IO) {
            try {
                prefs.edit().putBoolean("setup_complete", userSettings.setupComplete).apply()

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
        val disabledSettings = currentSettings.copy(notificationsEnabled = false)
        saveSettings(disabledSettings)
    }
}
