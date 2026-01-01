package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.Flow

expect class UserSettingsRepositoryImpl(
    authRepository: AuthRepository
) : UserSettingsRepository {
    override fun getSettings(): Flow<UserSettings>
    override suspend fun saveSettings(userSettings: UserSettings)
    override suspend fun syncUserSettings()
}
