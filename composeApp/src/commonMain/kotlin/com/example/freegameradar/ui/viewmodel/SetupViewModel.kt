package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SetupViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val authRepository: AuthRepository
) : KmpViewModel() {

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    init {
        userSettingsRepository.getSettings()
            .onEach { _userSettings.value = it }
            .launchIn(viewModelScope)
    }

    suspend fun savePreferencesAndCompleteSetup(
        notificationsEnabled: Boolean,
        preferredGamePlatforms: List<String>,
        preferredGameTypes: List<String>
    ) {
        val newSettings = UserSettings(
            notificationsEnabled = notificationsEnabled,
            preferredGamePlatforms = preferredGamePlatforms,
            preferredGameTypes = preferredGameTypes,
            setupComplete = true // Set setup as complete
        )
        userSettingsRepository.saveSettings(newSettings)
    }
}
