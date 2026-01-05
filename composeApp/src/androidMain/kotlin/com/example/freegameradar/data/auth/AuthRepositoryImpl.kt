package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

actual class AuthRepositoryImpl : AuthRepository {
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore

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

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
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

    override suspend fun signOut() {
        firebaseAuth.signOut()
    }

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            val user = firebaseAuth.currentUser ?: return Result.failure(IllegalStateException("User not logged in."))
            val userId = user.uid

            // First, delete the user's document from Firestore to ensure data is removed.
            firestore.collection("users").document(userId).delete().await()

            // Then, and only then, delete the user from Firebase Authentication.
            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            println("Error deleting account: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun linkAccount(email: String, password: String): Result<User> {
        return try {
            val credential = EmailAuthProvider.getCredential(email, password)
            val authResult = firebaseAuth.currentUser?.linkWithCredential(credential)?.await()
            Result.success(User(authResult?.user?.uid ?: "", authResult?.user?.email ?: "", false))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
