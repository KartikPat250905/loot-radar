package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.settings.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SetupUiState(
    val notificationsEnabled: Boolean = false,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList()
)

class SetupViewModel(
    private val userSettingsRepository: UserSettingsRepository
) : KmpViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    init {
        userSettingsRepository.getSettings()
            .onEach { settings ->
                _uiState.value = SetupUiState(
                    notificationsEnabled = settings.notificationsEnabled,
                    preferredGamePlatforms = settings.preferredGamePlatforms,
                    preferredGameTypes = settings.preferredGameTypes
                )
            }
            .launchIn(viewModelScope)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(notificationsEnabled = enabled)
    }

    fun togglePlatform(platform: String) {
        val currentPlatforms = _uiState.value.preferredGamePlatforms.toMutableList()
        if (currentPlatforms.contains(platform)) {
            currentPlatforms.remove(platform)
        } else {
            currentPlatforms.add(platform)
        }
        _uiState.value = _uiState.value.copy(preferredGamePlatforms = currentPlatforms)
    }

    fun toggleType(type: String) {
        val currentTypes = _uiState.value.preferredGameTypes.toMutableList()
        if (currentTypes.contains(type)) {
            currentTypes.remove(type)
        } else {
            currentTypes.add(type)
        }
        _uiState.value = _uiState.value.copy(preferredGameTypes = currentTypes)
    }

    fun completeSetup() {
        viewModelScope.launch {
            val newSettings = UserSettings(
                notificationsEnabled = _uiState.value.notificationsEnabled,
                preferredGamePlatforms = _uiState.value.preferredGamePlatforms,
                preferredGameTypes = _uiState.value.preferredGameTypes,
                setupComplete = true
            )
            userSettingsRepository.saveSettings(newSettings)
        }
    }
}
