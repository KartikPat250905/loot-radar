package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun continueAsGuest(): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun signInWithGoogle(idToken: String): Result<User>
    suspend fun signInWithMicrosoft(accessToken: String): Result<User>
}
