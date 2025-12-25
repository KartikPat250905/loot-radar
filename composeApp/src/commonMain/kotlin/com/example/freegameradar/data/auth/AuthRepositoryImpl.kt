package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import kotlinx.coroutines.flow.Flow

expect class AuthRepositoryImpl() : AuthRepository {
    override suspend fun login(email: String, password: String): Result<User>
    override suspend fun register(email: String, password: String): Result<User>
    override suspend fun continueAsGuest(): Result<User>
    override fun getAuthStateFlow(): Flow<User?>
}