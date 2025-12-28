package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun continueAsGuest(): Result<User>
    fun getAuthStateFlow(): Flow<User?>
    fun isUserLoggedIn(): Boolean
}
