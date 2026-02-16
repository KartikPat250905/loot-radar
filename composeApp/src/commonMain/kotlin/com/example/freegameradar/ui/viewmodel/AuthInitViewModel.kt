package com.radarlabs.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radarlabs.freegameradar.data.repository.UserSettingsRepository
import com.radarlabs.freegameradar.data.repository.UserStatsRepository
import com.radarlabs.freegameradar.util.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthInitViewModel(
    private val userSettingsRepository: UserSettingsRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    fun initialize() {
        viewModelScope.launch {
            try {
                Logger.d("AuthInitViewModel", "🔄 Starting sync...")

                userSettingsRepository.syncUserSettings()
                Logger.d("AuthInitViewModel", "✅ Settings synced")

                userStatsRepository.syncClaimedValue()
                Logger.d("AuthInitViewModel", "✅ Stats synced")

                _isInitialized.value = true
            } catch (e: Exception) {
                Logger.e("AuthInitViewModel", "❌ Sync failed: ", e)
                _isInitialized.value = true
            }
        }
    }
}
