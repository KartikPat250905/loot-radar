package com.example.freegameradar.data.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRUser
import com.example.freegameradar.data.models.User
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth = FIRAuth.auth()

    actual override suspend fun login(email: String, password: String): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInWithEmail(email, password) { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser()))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    actual override suspend fun register(email: String, password: String): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.createUserWithEmail(email, password) { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser()))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    actual override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.sendPasswordResetWithEmail(email) { error ->
            if (error == null) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(Exception(error.localizedDescription() ?: "Unknown error")))
            }
        }
    }


    actual override suspend fun continueAsGuest(): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInAnonymouslyWithCompletion { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser(isGuest = true)))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    actual override suspend fun signInAsGuest(): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInAnonymouslyWithCompletion { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser(isGuest = true)))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    actual override fun getAuthStateFlow(): Flow<User?> = callbackFlow {
        val handle = firebaseAuth.addAuthStateDidChangeListenerWithBlock { _, user ->
            trySend(user?.toUser()).isSuccess
        }
        awaitClose { firebaseAuth.removeAuthStateDidChangeListenerWithHandle(handle) }
    }

    actual override fun isUserLoggedIn(): Boolean {
        val user = firebaseAuth.currentUser
        return user != null && !user.isAnonymous()
    }

    actual override suspend fun signOut() {
        try {
            firebaseAuth.signOut(null)
        } catch (e: Exception) {
            println("Error signing out: ${e.message}")
        }
    }

    actual override suspend fun deleteAccount(): Result<Unit> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.currentUser?.deleteWithCompletion { error ->
            if (error == null) {
                continuation.resume(Result.success(Unit))
            } else {
                continuation.resume(Result.failure(Exception(error.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    actual override suspend fun linkAccount(email: String, password: String): Result<User> {
        // Not implemented for iOS
        return Result.failure(Exception("Not implemented for iOS"))
    }

    private fun FIRUser.toUser(isGuest: Boolean = false): User {
        return User(
            uid = uid(),
            email = email(),
            isGuest = isGuest || isAnonymous()
        )
    }
}
