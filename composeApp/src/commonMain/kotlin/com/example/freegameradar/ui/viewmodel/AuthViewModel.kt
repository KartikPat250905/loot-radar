package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.data.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(private val authRepository: AuthRepository) : KmpViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    val currentUser: StateFlow<User?> = authRepository.getAuthStateFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        viewModelScope.launch {
            authRepository.getAuthStateFlow().collect { user ->
                _authState.value = when {
                    user != null -> if (user.isAnonymous) AuthState.Guest else AuthState.LoggedIn
                    else -> AuthState.Error("Not Logged In")
                }
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(email, password)
            result.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown login error")
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(email, password)
            result.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown registration error")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.sendPasswordResetEmail(email)
            result.onSuccess {
                _authState.value = AuthState.Success(
                    "If your email is registered, you will receive a password reset link. " +
                            "If you don\'t have an account, please sign up."
                )
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error")
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.continueAsGuest()
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            GameDatabaseProvider.clearAllData()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.deleteAccount()
            result.onSuccess {
                GameDatabaseProvider.clearAllData()
            }.onFailure {
                _authState.value = AuthState.Error(it.message ?: "Unknown error during account deletion")
            }
        }
    }
}