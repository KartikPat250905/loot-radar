package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth = Firebase.auth

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user?.let { User(it.uid, it.email) }
                ?: return Result.failure(Exception("User is null after authentication"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user?.let { User(it.uid, it.email) }
                ?: return Result.failure(Exception("User is null after authentication"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun continueAsGuest(): Result<User> {
        return try {
            val authResult = firebaseAuth.signInAnonymously().await()
            val user = authResult.user?.let { User(it.uid, isGuest = true) }
                ?: return Result.failure(Exception("User is null"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.let { User(it.uid, it.email, it.isAnonymous) }
    }
}
