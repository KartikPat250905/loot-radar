package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

actual class AuthRepositoryImpl : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User> {
        return Result.success(User("desktop-user-id", email, false))
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return Result.success(User("desktop-user-id", email, false))
    }

    override suspend fun continueAsGuest(): Result<User> {
        return Result.success(User("desktop-guest-id", "Guest", true))
    }

    override suspend fun signInAsGuest(): Result<User> {
        return Result.success(User("desktop-guest-id", "Guest", true))
    }

    override fun getAuthStateFlow(): Flow<User?> {
        return flowOf(User("desktop-guest-id", "Guest", true))
    }

    override fun isUserLoggedIn(): Boolean {
        return true
    }
}
