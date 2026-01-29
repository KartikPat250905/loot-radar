package com.example.freegameradar.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.data.models.User
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val isGuest: Boolean = true
)

class SettingsViewModel(
    private val authRepository: AuthRepository,
    private val settingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var settingsOpenCount = 0

    private val _showSettingsAd = Channel<Unit>(Channel.BUFFERED)
    val showSettingsAd = _showSettingsAd.receiveAsFlow()

    init {
        loadUserData()
    }

    fun onSettingsScreenOpen() {
        settingsOpenCount++
        Log.d("SettingsViewModel", "⚙️ Settings opened: count=$settingsOpenCount")

        if (settingsOpenCount % 8 == 0) {
            Log.d("SettingsViewModel", "🎯 TRIGGERING AD! Count=$settingsOpenCount")
            viewModelScope.launch {
                _showSettingsAd.send(Unit)
                Log.d("SettingsViewModel", "✅ Ad emission complete")
            }
        } else {
            Log.d("SettingsViewModel", "⏭️ Not showing ad (count % 3 != 0)")
        }
    }

    private fun loadUserData() {
        authRepository.getAuthStateFlow()
            .onEach { user ->
                _uiState.value = SettingsUiState(
                    user = user,
                    isGuest = user?.isAnonymous ?: true
                )
            }
            .launchIn(viewModelScope)
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun deleteAccount(onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            onResult(result)
        }
    }

    fun upgradeAccount(email: String, password: String, onResult: (Result<User>) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.linkAccount(email, password)
            result.onSuccess { updatedUser ->
                _uiState.value = SettingsUiState(
                    user = updatedUser,
                    isGuest = updatedUser.isAnonymous
                )
            }
            onResult(result)
        }
    }
}
