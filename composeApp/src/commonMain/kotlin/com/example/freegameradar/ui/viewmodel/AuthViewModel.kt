package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.data.auth.getCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    init {
        authRepository.getAuthStateFlow()
            .onEach { user ->
                _authState.value = when {
                    user == null -> AuthState.Error("Not logged in")
                    user.isAnonymous -> AuthState.Guest
                    else -> AuthState.LoggedIn
                }
            }
            .launchIn(CoroutineScope(getCoroutineContext()))
    }

    fun login(email: String, password: String) {
        CoroutineScope(getCoroutineContext()).launch {
            authRepository.login(email, password)
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
        }
    }
    
    fun register(email: String, password: String) {
        CoroutineScope(getCoroutineContext()).launch {
             authRepository.register(email, password)
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Registration failed")
                }
        }
    }

    fun continueAsGuest() {
        CoroutineScope(getCoroutineContext()).launch {
            authRepository.continueAsGuest()
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Guest sign-in failed")
                }
        }
    }

    fun showNotImplementedError() {
        _authState.value = AuthState.Error("This feature is not yet implemented.")
    }
}
