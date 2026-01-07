package com.example.freegameradar.firebase

import com.example.freegameradar.ui.validation.ValidationUtils
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
                    val userFriendlyError = FirebaseErrorMapper.mapError(errorResponse.error)
                    Result.failure(FirebaseAuthException(userFriendlyError))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Request failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Sign in exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Sign up with email and password (already exists, enhanced with error mapping)
     */
    suspend fun signUp(email: String, password: String): Result<FirebaseAuthResponse> {
        return try {
            // Validate inputs before API call
            if (!ValidationUtils.isValidEmail(email)) {
                return Result.failure(FirebaseAuthException("Please enter a valid email"))
            }
            if (!ValidationUtils.isValidPassword(password)) {
                return Result.failure(FirebaseAuthException("Password must be at least 6 characters"))
            }

            println("📝 Attempting sign up for: $email")

            val response: HttpResponse = client.post(FirebaseConfig.SIGN_UP_URL) {
                contentType(ContentType.Application.Json)
                setBody(SignUpRequest(
                    email = email.trim(),
                    password = password,
                    returnSecureToken = true
                ))
            }

            if (response.status.isSuccess()) {
                val authResponse = response.body<FirebaseAuthResponse>()
                println("✅ Sign up successful: ${authResponse.email}")

                // Auto-save tokens to storage
                TokenStorage.saveAuthResponse(authResponse)

                Result.success(authResponse)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Sign up failed: ${response.status} - $errorBody")

                try {
                    val errorResponse = response.body<FirebaseErrorResponse>()
                    val userFriendlyError = FirebaseErrorMapper.mapError(errorResponse.error)
                    Result.failure(FirebaseAuthException(userFriendlyError))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Sign up failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Sign up exception: ${e.message}")
            e.printStackTrace()
            val userFriendlyError = FirebaseErrorMapper.mapException(e)
            Result.failure(FirebaseAuthException(userFriendlyError))
        }
    }
    suspend fun refreshToken(refreshToken: String? = null): Result<RefreshTokenResponse> {
        return try {
            val tokenToRefresh = refreshToken ?: TokenStorage.getRefreshToken()

            if (tokenToRefresh == null) {
                println("❌ No refresh token available")
                return Result.failure(FirebaseAuthException("No refresh token available"))
            }

            println("🔄 Refreshing authentication token...")

            val response: HttpResponse = client.post(FirebaseConfig.REFRESH_TOKEN_URL) {
                contentType(ContentType.Application.Json)
                setBody(RefreshTokenRequest(refreshToken = tokenToRefresh))
            }

            if (response.status.isSuccess()) {
                val tokenResponse = response.body<RefreshTokenResponse>()
                println("✅ Token refreshed successfully")
                println("   - New token expires in: ${tokenResponse.expiresIn} seconds")

                // UPDATE STORAGE AUTOMATICALLY
                TokenStorage.saveIdToken(tokenResponse.idToken)
                TokenStorage.saveRefreshToken(tokenResponse.refreshToken)
                TokenStorage.saveTokenExpiry(tokenResponse.expiresIn)

                Result.success(tokenResponse)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Token refresh failed: ${response.status} - $errorBody")

                // If refresh fails, tokens are invalid - clear them
                TokenStorage.clearAll()

                try {
                    val errorResponse = response.body<FirebaseErrorResponse>()
                    val userFriendlyError = FirebaseErrorMapper.mapError(errorResponse.error)
                    Result.failure(FirebaseAuthException(userFriendlyError))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Request failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Token refresh exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Refresh token if expired or about to expire (within 5 minutes)
     * Returns true if refresh was performed successfully, false if not needed
     */
    suspend fun refreshTokenIfNeeded(): Result<Boolean> {
        if (!TokenStorage.isTokenExpiringSoon()) {
            println("🔍 Token is still valid, no refresh needed")
            return Result.success(false)
        }

        println("⚠️ Token is expired or expiring soon, refreshing...")
        return refreshToken().map { true }
    }

    /**
     * Check if user is logged in with valid token
     * Auto-refreshes if token is expired but refresh token is available
     */
    suspend fun isLoggedInWithValidToken(): Boolean {
        if (!TokenStorage.hasValidSession()) {
            println("🔍 No valid session found")
            return false
        }

        // Try to refresh if needed
        if (TokenStorage.isTokenExpired()) {
            println("🔄 Token expired, attempting auto-refresh...")
            val refreshResult = refreshToken()
            return refreshResult.isSuccess
        }

        return true
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
                    val userFriendlyError = FirebaseErrorMapper.mapError(errorResponse.error)
                    Result.failure(FirebaseAuthException(userFriendlyError))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Request failed: ${response.status}"))
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
                    val userFriendlyError = FirebaseErrorMapper.mapError(errorResponse.error)
                    Result.failure(FirebaseAuthException(userFriendlyError))
                } catch (e: Exception) {
                    Result.failure(FirebaseAuthException("Request failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Account deletion exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
}

/**
 * Custom exception for Firebase authentication errors
 */
class FirebaseAuthException(message: String) : Exception(message)
