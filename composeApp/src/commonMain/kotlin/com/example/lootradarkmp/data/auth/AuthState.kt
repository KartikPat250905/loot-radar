package com.example.lootradarkmp.data.auth

import com.example.lootradarkmp.data.models.User

sealed class AuthState {
    object Loading : AuthState()
    object Guest : AuthState()
    data class LoggedIn(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}
