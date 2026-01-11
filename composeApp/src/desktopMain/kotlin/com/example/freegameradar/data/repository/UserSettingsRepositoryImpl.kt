package com.example.freegameradar.data.repository

import com.example.freegameradar.core.createSettings
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.settings.UserSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository
) : UserSettingsRepository {

    private val settings: Settings = createSettings()

    override fun getSettings(): Flow<UserSettings> {
        val currentSettings = UserSettings(
            notificationsEnabled = settings.get("notificationsEnabled", false),
            preferredGamePlatforms = Json.decodeFromString(
                settings.get("preferredGamePlatforms", "[]")
            ),
            preferredGameTypes = Json.decodeFromString(
                settings.get("preferredGameTypes", "[]")
            ),
            setupComplete = settings.get("setupComplete", false)
        )
        return flowOf(currentSettings)
    }

    override suspend fun saveSettings(userSettings: UserSettings) {
        settings.putBoolean("notificationsEnabled", userSettings.notificationsEnabled)
        settings.putString("preferredGamePlatforms", Json.encodeToString(userSettings.preferredGamePlatforms))
        settings.putString("preferredGameTypes", Json.encodeToString(userSettings.preferredGameTypes))
        settings.putBoolean("setupComplete", userSettings.setupComplete)
    }

    override suspend fun syncUserSettings() {
        // No-op for desktop as settings are not tied to a remote user account.
        println("Desktop dummy: syncing user settings")
    }

    override suspend fun disableAllNotifications() {
        // No-op on desktop
        println("Desktop dummy: disabling all notifications")
    }
}