package com.example.freegameradar.data.repository

import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface UserSettingsRepository {

    fun getSettings(): Flow<UserSettings>

    suspend fun saveSettings(userSettings: UserSettings)

    suspend fun syncUserSettings()

    suspend fun disableAllNotifications()
    val hasCompletedInitialSync: StateFlow<Boolean>
}
