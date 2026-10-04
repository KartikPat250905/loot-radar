package com.radarlabs.freegameradar.data.auth

sealed interface AuthState {
    data object Loading : AuthState
    data object Idle : AuthState
    data object Submitting : AuthState
    data object LoggedIn : AuthState
    data object Guest : AuthState
    data class Success(val message: String) : AuthState
    data class Error(val message: String) : AuthState
}