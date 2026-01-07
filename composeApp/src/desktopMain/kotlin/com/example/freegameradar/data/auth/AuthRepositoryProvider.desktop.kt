package com.example.freegameradar.data.auth

import com.example.freegameradar.data.models.User
import com.example.freegameradar.firebase.FirebaseAuthException
import com.example.freegameradar.firebase.FirebaseAuthService
import com.example.freegameradar.firebase.TokenStorage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class DesktopAuthRepositoryImpl : AuthRepository {

    private val authService = FirebaseAuthService()
    private val authStateFlow = MutableStateFlow<User?>(null)

    init {
        // On startup, try to restore user from TokenStorage
        val stored = TokenStorage.getStoredUser()
        if (stored != null && TokenStorage.hasValidSession()) {
            authStateFlow.value = User(
                uid = stored.uid,
                email = stored.email,
                isAnonymous = false
            )
        } else {
            authStateFlow.value = null
        }
    }
    
    /**
     * Ensure valid token before operations
     * Auto-refreshes if token is expired
     */
    private suspend fun ensureValidToken(): Boolean {
        return authService.isLoggedInWithValidToken()
    }

    override suspend fun login(email: String, password: String): Result<User> {
        return try {
            val result = authService.signIn(email, password)
            result.fold(
                onSuccess = { response ->
                    val user = User(
                        uid = response.localId,
                        email = response.email ?: email,
                        isAnonymous = false
                    )
                    authStateFlow.value = user
                    Result.success(user)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, password: String): Result<User> {
        return try {
            val result = authService.signUp(email, password)
            result.fold(
                onSuccess = { response ->
                    val user = User(
                        uid = response.localId,
                        email = response.email ?: email,
                        isAnonymous = false
                    )
                    authStateFlow.value = user
                    Result.success(user)
                },
                onFailure = { error ->
                    Result.failure(error)
                }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return authService.sendPasswordResetEmail(email)
    }

    override suspend fun continueAsGuest(): Result<User> {
        val guestUser = User(
            uid = "guest",
            email = "Guest",
            isAnonymous = true
        )
        authStateFlow.value = guestUser
        TokenStorage.clearAll()
        return Result.success(guestUser)
    }

    override suspend fun signInAsGuest(): Result<User> {
        return continueAsGuest()
    }

    override fun getAuthStateFlow(): Flow<User?> = authStateFlow

    override fun isUserLoggedIn(): Boolean {
        return authStateFlow.value?.let { !it.isAnonymous } == true
    }

    override suspend fun signOut() {
        authService.signOut()
        authStateFlow.value = null
    }

    override suspend fun deleteAccount(): Result<Unit> {
        // Ensure valid token before deletion
        if (!ensureValidToken()) {
            return Result.failure(FirebaseAuthException("Session expired, please login again"))
        }
        
        val current = authStateFlow.value
        if (current == null || current.isAnonymous) {
            return Result.failure(IllegalStateException("User not logged in or guest."))
        }

        val token = TokenStorage.getIdToken()
        if (token == null) {
            return Result.failure(FirebaseAuthException("No ID token available"))
        }

        val result = authService.deleteAccount(token)
        result.onSuccess {
            authStateFlow.value = null
        }
        return result
    }

    override suspend fun linkAccount(email: String, password: String): Result<User> {
        return Result.failure(
            UnsupportedOperationException("Linking accounts not implemented for Desktop")
        )
    }
}

actual fun createAuthRepository(): AuthRepository = DesktopAuthRepositoryImpl()