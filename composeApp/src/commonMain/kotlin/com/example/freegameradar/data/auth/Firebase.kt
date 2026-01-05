package com.example.freegameradar.data.auth

import kotlin.coroutines.CoroutineContext

// Platform-agnostic interface instead of FirebaseAuth type
interface FirebaseAuthWrapper {
    suspend fun signIn(email: String, password: String): AuthResult?
    suspend fun signUp(email: String, password: String): AuthResult?
    suspend fun signOut()
    fun getCurrentUser(): UserInfo?
}

data class UserInfo(
    val uid: String,
    val email: String?
)

data class AuthResult(
    val user: UserInfo
)

expect fun getFirebaseAuth(): FirebaseAuthWrapper
expect fun getCoroutineContext(): CoroutineContext
