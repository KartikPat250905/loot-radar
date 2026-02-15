package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.data.models.User
import com.example.freegameradar.data.repository.UserStatsRepository
import com.example.freegameradar.util.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val userStatsRepository: UserStatsRepository
) : ViewModel() {

    private val _authState = kotlinx.coroutines.flow.MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    val currentUser: StateFlow<User?> = authRepository.getAuthStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                _authState.value = when {
                    user == null -> AuthState.Error("Not logged in")
                    user.isAnonymous -> AuthState.Guest
                    else -> {
                        Logger.d("AuthViewModel", "User logged in, syncing stats...")
                        userStatsRepository.syncClaimedValue()
                        AuthState.LoggedIn
                    }
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting login...")
            val result = authRepository.login(email, password)
            if (result.isSuccess) {
                Logger.d("AuthViewModel", "Login successful, syncing stats...")
                userStatsRepository.syncClaimedValue()
                // Don't set Success here, let checkAuthState() handle it
                // The flow will automatically update to LoggedIn
            } else {
                Logger.e("AuthViewModel", "Login failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }


    fun signUp(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Attempting sign up...")
            val result = authRepository.register(email, password)
            if (result.isSuccess) {
                Logger.d("AuthViewModel", "Sign up successful, syncing stats...")
                userStatsRepository.syncClaimedValue()
                _authState.value = AuthState.Success("Account created successfully!")
            } else {
                Logger.e("AuthViewModel", "Sign up failed: ${result.exceptionOrNull()?.message}")
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Sign up failed")
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            Logger.d("AuthViewModel", "Continuing as guest...")
            val result = authRepository.signInAsGuest()
            if (result.isSuccess) {
                _authState.value = AuthState.Guest
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to continue as guest")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            Logger.d("AuthViewModel", "Sending password reset email...")
            val result = authRepository.sendPasswordResetEmail(email)
            if (result.isSuccess) {
                _authState.value = AuthState.Success("Password reset email sent!")
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Failed to send reset email")
            }
        }
    }
}
