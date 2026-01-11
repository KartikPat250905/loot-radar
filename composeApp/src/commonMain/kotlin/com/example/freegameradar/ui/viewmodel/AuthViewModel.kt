package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.data.models.User
import com.example.freegameradar.firebase.FirebaseErrorMapper
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
                val userFriendlyError = FirebaseErrorMapper.mapException(it)
                _authState.value = AuthState.Error(userFriendlyError)
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(email, password)
            result.onFailure {
                val userFriendlyError = FirebaseErrorMapper.mapException(it)
                _authState.value = AuthState.Error(userFriendlyError)
            }
        }
    }

    fun continueAsGuest() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            authRepository.continueAsGuest()
        }
    }
}