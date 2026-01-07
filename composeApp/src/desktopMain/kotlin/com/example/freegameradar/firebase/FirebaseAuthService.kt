package com.example.freegameradar.firebase

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Desktop Firebase Authentication Service using REST API
 * Android uses native Firebase SDK instead
 */
class FirebaseAuthService {

    private val client = FirebaseHttpClient.client

    /**
     * Sign in with email and password
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseAuthResponse> {
        return try {
            println("🔐 Attempting sign in for: $email")

            val response: HttpResponse = client.post(FirebaseConfig.SIGN_IN_URL) {
                contentType(ContentType.Application.Json)
                setBody(SignInRequest(
                    email = email,
                    password = password,
                    returnSecureToken = true
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<FirebaseAuthResponse>()
                println("✅ Sign in successful: ${authResponse.email}")

                // ADD THIS - Save tokens to storage
                TokenStorage.saveAuthResponse(authResponse)

                Result.success(authResponse)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Sign in failed: ${response.status} - $errorBody")

                try {
                    val errorResponse = response.body<FirebaseErrorResponse>()
                    Result.failure(FirebaseAuthException(errorResponse.error.message))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Sign in failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Sign in exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Sign up with email and password
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseAuthResponse> {
        return try {
            println("📝 Attempting sign up for: $email")

            val response: HttpResponse = client.post(FirebaseConfig.SIGN_UP_URL) {
                contentType(ContentType.Application.Json)
                setBody(SignUpRequest(
                    email = email,
                    password = password,
                    returnSecureToken = true
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<FirebaseAuthResponse>()
                println("✅ Sign up successful: ${authResponse.email}")

                // ADD THIS - Save tokens to storage
                TokenStorage.saveAuthResponse(authResponse)

                Result.success(authResponse)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Sign up failed: ${response.status} - $errorBody")

                try {
                    val errorResponse = response.body<FirebaseErrorResponse>()
                    Result.failure(FirebaseAuthException(errorResponse.error.message))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Sign up failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Sign up exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Sign out - clear stored tokens
     */
    fun signOut() {
        println("👋 Signing out...")
        TokenStorage.clearAll()
    }

    /**
     * Get current user from storage
     */
    fun getCurrentUser(): StoredUser? {
        return TokenStorage.getStoredUser()
    }

    /**
     * Check if user is currently logged in
     */
    fun isLoggedIn(): Boolean {
        return TokenStorage.hasValidSession()
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            println("📧 Sending password reset email to: $email")

            val response: HttpResponse = client.post(FirebaseConfig.PASSWORD_RESET_URL) {
                contentType(ContentType.Application.Json)
                setBody(PasswordResetRequest(email = email))
            }

            if (response.status.isSuccess()) {
                println("✅ Password reset email sent")
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Password reset failed: ${response.status} - $errorBody")

                try {
                    val errorResponse = response.body<FirebaseErrorResponse>()
                    Result.failure(FirebaseAuthException(errorResponse.error.message))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Password reset failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Password reset exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Delete user account
     */
    suspend fun deleteAccount(idToken: String): Result<Unit> {
        return try {
            println("🗑️ Attempting to delete account")

            val response: HttpResponse = client.post(FirebaseConfig.DELETE_ACCOUNT_URL) {
                contentType(ContentType.Application.Json)
                setBody(DeleteAccountRequest(idToken = idToken))
            }

            if (response.status.isSuccess()) {
                println("✅ Account deleted successfully")

                // ADD THIS - Clear tokens after deletion
                TokenStorage.clearAll()

                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Account deletion failed: ${response.status} - $errorBody")

                try {
                    val errorResponse = response.body<FirebaseErrorResponse>()
                    Result.failure(FirebaseAuthException(errorResponse.error.message))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Account deletion failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Account deletion exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Refresh ID token using refresh token
     */
    suspend fun refreshToken(refreshToken: String? = null): Result<RefreshTokenResponse> {
        return try {
            val tokenToRefresh = refreshToken ?: TokenStorage.getRefreshToken()

            if (tokenToRefresh == null) {
                return Result.failure(FirebaseAuthException("No refresh token available"))
            }

            println("🔄 Refreshing authentication token")

            val response: HttpResponse = client.post(FirebaseConfig.REFRESH_TOKEN_URL) {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken = tokenToRefresh))
            }

            if (response.status.isSuccess()) {
                val tokenResponse = response.body<RefreshTokenResponse>()
                println("✅ Token refreshed successfully")

                // ADD THIS - Update stored tokens
                TokenStorage.saveIdToken(tokenResponse.idToken)
                TokenStorage.saveRefreshToken(tokenResponse.refreshToken)
                TokenStorage.saveTokenExpiry(tokenResponse.expiresIn)

                Result.success(tokenResponse)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Token refresh failed: ${response.status} - $errorBody")

                // If refresh fails, clear all tokens
                TokenStorage.clearAll()

                Result.failure(FirebaseAuthException("Token refresh failed"))
            }
        } catch (e: Exception) {
            println("❌ Token refresh exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

/**
 * Custom exception for Firebase authentication errors
 */
class FirebaseAuthException(message: String) : Exception(message)
