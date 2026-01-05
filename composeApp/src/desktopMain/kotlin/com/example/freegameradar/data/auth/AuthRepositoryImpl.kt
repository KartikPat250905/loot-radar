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

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        println("Desktop dummy: sending password reset to $email")
        return Result.success(Unit)
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

    override suspend fun signOut() {
        println("Desktop dummy: signing out")
    }

    override suspend fun deleteAccount(): Result<Unit> {
        println("Desktop dummy: deleting account")
        return Result.success(Unit)
    }

    override suspend fun linkAccount(email: String, password: String): Result<User> {
        return Result.success(User("desktop-user-id", email, false))
    }
}