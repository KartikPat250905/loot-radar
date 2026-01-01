package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

data class SettingsUiState(
    val user: User? = null,
    val isGuest: Boolean = true
)

class SettingsViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
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

    fun deleteAccount() {
        viewModelScope.launch {
            authRepository.deleteAccount()
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
