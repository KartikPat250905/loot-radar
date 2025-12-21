package com.example.lootradarkmp.ui.viewmodel

import com.example.lootradarkmp.data.auth.AuthRepository
import com.example.lootradarkmp.data.auth.AuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState

    fun checkAuth() {
        scope.launch {
            val user = authRepository.getCurrentUser()
            _authState.value =
                if (user != null) AuthState.LoggedIn(user)
                else AuthState.Guest
        }
    }

    fun continueAsGuest() {
        scope.launch {
            _authState.value = AuthState.Guest
        }
    }
}
