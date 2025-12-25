package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

actual class AuthRepositoryImpl : AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    actual override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(User(authResult.user?.uid ?: "", authResult.user?.email ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(User(authResult.user?.uid ?: "", authResult.user?.email ?: ""))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun continueAsGuest(): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.signInAnonymously().await()
            Result.success(User(authResult.user?.uid ?: "", "Guest"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    actual override suspend fun getCurrentUser(): User? {
        val firebaseUser = firebaseAuth.currentUser
        return firebaseUser?.let { User(it.uid, it.email ?: "") }
    }
}