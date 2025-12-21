package com.example.lootradarkmp.data.auth

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIRAuthDataResult
import cocoapods.FirebaseAuth.FIRUser
import com.example.lootradarkmp.data.models.User
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSError
import kotlin.coroutines.resume

class AuthRepositoryImpl : AuthRepository {

    private val firebaseAuth = FIRAuth.auth()

    override suspend fun login(email: String, password: String): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInWithEmail(email, password) { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser()))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    override suspend fun register(email: String, password: String): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.createUserWithEmail(email, password) { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser()))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    override suspend fun continueAsGuest(): Result<User> = suspendCancellableCoroutine { continuation ->
        firebaseAuth.signInAnonymouslyWithCompletion { authResult, error ->
            if (authResult != null) {
                continuation.resume(Result.success(authResult.user.toUser(isGuest = true)))
            } else {
                continuation.resume(Result.failure(Exception(error?.localizedDescription() ?: "Unknown error")))
            }
        }
    }

    override suspend fun getCurrentUser(): User? {
        return firebaseAuth.currentUser?.toUser()
    }

    private fun FIRUser.toUser(isGuest: Boolean = false): User {
        return User(
            uid = uid(),
            email = email(),
            isGuest = isGuest || isAnonymous()
        )
    }
}
