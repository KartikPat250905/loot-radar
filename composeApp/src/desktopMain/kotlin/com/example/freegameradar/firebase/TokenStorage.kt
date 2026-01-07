package com.example.freegameradar.firebase

import java.util.prefs.Preferences

/**
 * Token storage for Desktop using Java Preferences API
 * Android uses native Firebase SDK which handles token storage automatically
 */
object TokenStorage {

    private val prefs = Preferences.userRoot().node("com.example.freegameradar.auth")

    // Keys for storage
    private const val KEY_ID_TOKEN = "idToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_ID = "userId"
    private const val KEY_TOKEN_EXPIRY = "tokenExpiry"

    /**
     * Save ID token
     */
    fun saveIdToken(token: String) {
        prefs.put(KEY_ID_TOKEN, token)
        prefs.flush()
        println("💾 Saved ID token")
    }

    /**
     * Save refresh token
     */
    fun saveRefreshToken(token: String) {
        prefs.put(KEY_REFRESH_TOKEN, token)
        prefs.flush()
        println("💾 Saved refresh token")
    }

    /**
     * Save user email
     */
    fun saveUserEmail(email: String) {
        prefs.put(KEY_USER_EMAIL, email)
        prefs.flush()
        println("💾 Saved user email: $email")
    }

    /**
     * Save user ID
     */
    fun saveUserId(userId: String) {
        prefs.put(KEY_USER_ID, userId)
        prefs.flush()
        println("💾 Saved user ID: $userId")
    }

    /**
     * Save token expiry time (current time + expiresIn seconds)
     */
    fun saveTokenExpiry(expiresInSeconds: String) {
        try {
            val expiryTime = System.currentTimeMillis() + (expiresInSeconds.toLong() * 1000)
            prefs.putLong(KEY_TOKEN_EXPIRY, expiryTime)
            prefs.flush()
            println("💾 Saved token expiry: $expiryTime")
        } catch (e: Exception) {
            println("⚠️ Failed to save token expiry: ${e.message}")
        }
    }

    /**
     * Save complete authentication response
     */
    fun saveAuthResponse(response: FirebaseAuthResponse) {
        saveIdToken(response.idToken)
        saveRefreshToken(response.refreshToken)
        response.email?.let { saveUserEmail(it) }
        saveUserId(response.localId)
        saveTokenExpiry(response.expiresIn)
        println("✅ Saved complete auth response")
    }

    /**
     * Get ID token
     */
    fun getIdToken(): String? {
        return prefs.get(KEY_ID_TOKEN, null)
    }

    /**
     * Get refresh token
     */
    fun getRefreshToken(): String? {
        return prefs.get(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Get user email
     */
    fun getUserEmail(): String? {
        return prefs.get(KEY_USER_EMAIL, null)
    }

    /**
     * Get user ID
     */
    fun getUserId(): String? {
        return prefs.get(KEY_USER_ID, null)
    }

    /**
     * Get token expiry time
     */
    fun getTokenExpiry(): Long {
        return prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
    }

    /**
     * Check if token is expired
     */
    fun isTokenExpired(): Boolean {
        val expiry = getTokenExpiry()
        if (expiry == 0L) return true

        val isExpired = System.currentTimeMillis() >= expiry
        if (isExpired) {
            println("⚠️ Token has expired")
        }
        return isExpired
    }

    /**
     * Check if user is logged in (has valid tokens)
     */
    fun hasValidSession(): Boolean {
        val hasIdToken = getIdToken() != null
        val hasRefreshToken = getRefreshToken() != null
        val notExpired = !isTokenExpired()

        val isValid = hasIdToken && hasRefreshToken && notExpired
        println("🔍 Session check: hasIdToken=$hasIdToken, hasRefreshToken=$hasRefreshToken, notExpired=$notExpired")
        return isValid
    }

    /**
     * Get stored user info if available
     */
    fun getStoredUser(): StoredUser? {
        val userId = getUserId()
        val email = getUserEmail()
        val idToken = getIdToken()

        return if (userId != null && email != null && idToken != null) {
            StoredUser(userId, email, idToken)
        } else {
            null
        }
    }

    /**
     * Clear all stored tokens (logout)
     */
    fun clearAll() {
        prefs.clear()
        prefs.flush()
        println("🗑️ Cleared all stored tokens")
    }

    /**
     * Print current storage state (for debugging)
     */
    fun printStorageState() {
        println("\n=== Token Storage State ===")
        println("User ID: ${getUserId() ?: "None"}")
        println("Email: ${getUserEmail() ?: "None"}")
        println("Has ID Token: ${getIdToken() != null}")
        println("Has Refresh Token: ${getRefreshToken() != null}")
        println("Token Expired: ${isTokenExpired()}")
        println("Valid Session: ${hasValidSession()}")
        println("===========================\n")
    }
}

/**
 * Data class for stored user information
 */
data class StoredUser(
    val uid: String,
    val email: String,
    val idToken: String
)
