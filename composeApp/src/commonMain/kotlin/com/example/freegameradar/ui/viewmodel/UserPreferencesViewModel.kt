package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class UserPreferencesUiState(
    val notificationsEnabled: Boolean = false,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList()
)

class UserPreferencesViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserPreferencesUiState())
    val uiState: StateFlow<UserPreferencesUiState> = _uiState.asStateFlow()

    init {
        userSettingsRepository.getSettings()
            .onEach { settings ->
                _uiState.value = UserPreferencesUiState(
                    notificationsEnabled = settings.notificationsEnabled,
                    preferredGamePlatforms = settings.preferredGamePlatforms,
                    preferredGameTypes = settings.preferredGameTypes
                )
            }
            .launchIn(viewModelScope)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = userSettingsRepository.getSettings().first()
            val newSettings = currentSettings.copy(notificationsEnabled = enabled)
            userSettingsRepository.saveSettings(newSettings)
        }
    }

    fun updatePreferences(
        preferredGamePlatforms: List<String>,
        preferredGameTypes: List<String>
    ) {
        viewModelScope.launch {
            val currentSettings = userSettingsRepository.getSettings().first()
            val newSettings = currentSettings.copy(
                preferredGamePlatforms = preferredGamePlatforms,
                preferredGameTypes = preferredGameTypes
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
