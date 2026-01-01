package com.example.freegameradar.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrDefault
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.db.User_settings
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository
) : UserSettingsRepository {

    private val db = GameDatabaseProvider.getDatabase().user_settingsQueries

    override fun getSettings(): Flow<UserSettings> = flow {
        // For now, iOS implementation will only use local DB
        // TODO: Add Firebase support for iOS
        val localSettings = db.getSettings().asFlow().mapToOneOrDefault(User_settings(0, 1L, "", ""), Dispatchers.IO).first()
        emit(UserSettings(
            notificationsEnabled = localSettings.notifications_enabled == 1L,
            preferredGamePlatforms = localSettings.preferred_game_platforms.split(",").filter { it.isNotEmpty() },
            preferredGameTypes = localSettings.preferred_game_types.split(",").filter { it.isNotEmpty() }
        ))
    }.flowOn(Dispatchers.IO)

    override suspend fun saveSettings(userSettings: UserSettings) {
        withContext(Dispatchers.IO) {
            db.insertSettings(
                notifications_enabled = if (userSettings.notificationsEnabled) 1L else 0L,
                preferred_game_platforms = userSettings.preferredGamePlatforms.joinToString(","),
                preferred_game_types = userSettings.preferredGameTypes.joinToString(",")
            )
            // TODO: Add Firebase support for iOS
        }
    }

    override suspend fun syncUserSettings() {
        // Not implemented for iOS
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
