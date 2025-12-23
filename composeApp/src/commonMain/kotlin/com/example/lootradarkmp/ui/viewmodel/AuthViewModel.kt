package com.example.lootradarkmp.ui.viewmodel

import com.example.lootradarkmp.data.auth.AuthRepository
import com.example.lootradarkmp.data.auth.AuthState
import com.example.lootradarkmp.data.auth.getCoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) {
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    fun checkAuthState() {
        _authState.value = AuthState.Loading

        CoroutineScope(getCoroutineContext()).launch {
            val user = authRepository.getCurrentUser()
            _authState.value =
                if (user != null) AuthState.LoggedIn
                else AuthState.Error("Not logged in")
        }
    }

    fun login(email: String, password: String) {
        CoroutineScope(getCoroutineContext()).launch {
            authRepository.login(email, password)
                .onSuccess {
                    _authState.value = AuthState.LoggedIn
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Login failed")
                }
        }
    }
    
    fun register(email: String, password: String) {
        CoroutineScope(getCoroutineContext()).launch {
             authRepository.register(email, password)
                .onSuccess {
                    _authState.value = AuthState.LoggedIn
                }
                .onFailure {
                    _authState.value = AuthState.Error(it.message ?: "Registration failed")
                }
        }
    }

    fun continueAsGuest() {
        CoroutineScope(getCoroutineContext()).launch {
            authRepository.continueAsGuest()
            _authState.value = AuthState.Guest
        }
    }
}
