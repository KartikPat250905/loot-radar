package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

actual class UserSettingsRepositoryImpl actual constructor(
    private val authRepository: AuthRepository
) : UserSettingsRepository {
    override fun getSettings(): Flow<UserSettings> = flow {
        // TODO: Implement for iOS with offline support
        emit(UserSettings())
    }

    override suspend fun saveSettings(userSettings: UserSettings) {
        // TODO: Implement for iOS with offline support
    }
}
