package com.example.lootradarkmp.data.auth

import com.example.lootradarkmp.data.models.User

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    suspend fun register(email: String, password: String): Result<User>
    suspend fun continueAsGuest(): Result<User>
    suspend fun getCurrentUser(): User?
}
