package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class UserPreferencesUiState(
    val notificationsEnabled: Boolean = false,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList(),
    val setupComplete: Boolean = false
)

class UserPreferencesViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : KmpViewModel() {

    private val _uiState = MutableStateFlow(UserPreferencesUiState())
    val uiState: StateFlow<UserPreferencesUiState> = _uiState.asStateFlow()

    init {
        userSettingsRepository.getSettings()
            .onEach { settings ->
                _uiState.value = UserPreferencesUiState(
                    notificationsEnabled = settings.notificationsEnabled,
                    preferredGamePlatforms = settings.preferredGamePlatforms,
                    preferredGameTypes = settings.preferredGameTypes,
                    setupComplete = settings.setupComplete
                )
            }
            .launchIn(viewModelScope)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentUiState = _uiState.value
            val newSettings = UserSettings(
                notificationsEnabled = enabled,
                preferredGamePlatforms = currentUiState.preferredGamePlatforms,
                preferredGameTypes = currentUiState.preferredGameTypes,
                setupComplete = currentUiState.setupComplete
            )
            userSettingsRepository.saveSettings(newSettings)
        }
    }

    fun updatePreferences(
        preferredGamePlatforms: List<String>,
        preferredGameTypes: List<String>
    ) {
        viewModelScope.launch {
            val currentUiState = _uiState.value
            val newSettings = UserSettings(
                notificationsEnabled = currentUiState.notificationsEnabled,
                preferredGamePlatforms = preferredGamePlatforms,
                preferredGameTypes = preferredGameTypes,
                setupComplete = currentUiState.setupComplete
            )
            userSettingsRepository.saveSettings(newSettings)
        }
    }

    fun disableAllNotifications() {
        viewModelScope.launch {
            userSettingsRepository.disableAllNotifications()
        }
    }
}
