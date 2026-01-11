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

}

actual fun createAuthRepository(): AuthRepository = DesktopAuthRepositoryImpl()