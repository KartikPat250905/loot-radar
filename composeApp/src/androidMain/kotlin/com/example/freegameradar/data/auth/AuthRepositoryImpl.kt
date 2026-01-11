package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual class AuthRepositoryImpl : AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.success(User(authResult.user?.uid ?: "", authResult.user?.email ?: "", false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            Result.success(User(authResult.user?.uid ?: "", authResult.user?.email ?: "", false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun continueAsGuest(): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.signInAnonymously().await()
            Result.success(User(authResult.user?.uid ?: "", "Guest", true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun signInAsGuest(): Result<User> {
        return try {
            val authResult: AuthResult = firebaseAuth.signInAnonymously().await()
            Result.success(User(authResult.user?.uid ?: "", "Guest", true))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAuthStateFlow(): Flow<User?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser?.let { User(it.uid, it.email ?: "", it.isAnonymous) }
            trySend(user)
        }
        firebaseAuth.addAuthStateListener(authStateListener)
        awaitClose { firebaseAuth.removeAuthStateListener(authStateListener) }
    }

    override fun isUserLoggedIn(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null && !user.isAnonymous
    }
}
