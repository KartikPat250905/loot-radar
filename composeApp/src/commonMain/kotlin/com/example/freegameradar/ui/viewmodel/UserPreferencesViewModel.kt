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
        val newUiState = _uiState.value.copy(notificationsEnabled = enabled)
        _uiState.value = newUiState

        viewModelScope.launch {
            val newSettings = UserSettings(
                notificationsEnabled = newUiState.notificationsEnabled,
                preferredGamePlatforms = newUiState.preferredGamePlatforms,
                preferredGameTypes = newUiState.preferredGameTypes,
                setupComplete = newUiState.setupComplete
            )
            userSettingsRepository.saveSettings(newSettings)
        }
    }

    fun updatePreferences(
        preferredGamePlatforms: List<String>,
        preferredGameTypes: List<String>
    ) {
        val newUiState = _uiState.value.copy(
            preferredGamePlatforms = preferredGamePlatforms,
            preferredGameTypes = preferredGameTypes
        )
        _uiState.value = newUiState

        viewModelScope.launch {
            val newSettings = UserSettings(
                notificationsEnabled = newUiState.notificationsEnabled,
                preferredGamePlatforms = newUiState.preferredGamePlatforms,
                preferredGameTypes = newUiState.preferredGameTypes,
                setupComplete = newUiState.setupComplete
            )
            userSettingsRepository.saveSettings(newSettings)
        }
    }

    fun disableAllNotifications() {
        _uiState.value = _uiState.value.copy(notificationsEnabled = false)

        viewModelScope.launch {
            userSettingsRepository.disableAllNotifications()
        }
    }
}
