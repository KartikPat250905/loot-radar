package com.example.lootradarkmp.data.auth

sealed interface AuthState {
    data object Loading : AuthState
    data object LoggedIn : AuthState
    data object Guest : AuthState
    data class Error(val message: String) : AuthState
}