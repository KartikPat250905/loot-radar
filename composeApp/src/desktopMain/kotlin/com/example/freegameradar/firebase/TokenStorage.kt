package com.example.freegameradar.firebase

import java.util.prefs.Preferences

object TokenStorage {

    private val prefs = Preferences.userRoot().node("com.example.freegameradar.auth")

    private const val KEY_ID_TOKEN = "idToken"
    private const val KEY_REFRESH_TOKEN = "refreshToken"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_USER_ID = "userId"
    private const val KEY_TOKEN_EXPIRY = "tokenExpiry"

    fun saveIdToken(token: String) {
        prefs.put(KEY_ID_TOKEN, token)
        prefs.flush()
        println("💾 Saved ID token")
    }

    fun saveRefreshToken(token: String) {
        prefs.put(KEY_REFRESH_TOKEN, token)
        prefs.flush()
        println("💾 Saved refresh token")
    }

    fun saveUserEmail(email: String) {
        prefs.put(KEY_USER_EMAIL, email)
        prefs.flush()
        println("💾 Saved user email: $email")
    }

    fun saveUserId(userId: String) {
        prefs.put(KEY_USER_ID, userId)
        prefs.flush()
        println("💾 Saved user ID: $userId")
    }

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

    fun saveAuthResponse(response: FirebaseAuthResponse) {
        saveIdToken(response.idToken)
        saveRefreshToken(response.refreshToken)
        response.email?.let { saveUserEmail(it) }
        saveUserId(response.localId)
        saveTokenExpiry(response.expiresIn)
        println("✅ Saved complete auth response")
    }

    fun getIdToken(): String? {
        return prefs.get(KEY_ID_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.get(KEY_REFRESH_TOKEN, null)
    }

    fun getUserEmail(): String? {
        return prefs.get(KEY_USER_EMAIL, null)
    }

    fun getUserId(): String? {
        return prefs.get(KEY_USER_ID, null)
    }

    fun getTokenExpiry(): Long {
        return prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
    }

    fun isTokenExpired(): Boolean {
        val expiry = getTokenExpiry()
        if (expiry == 0L) return true

        val isExpired = System.currentTimeMillis() >= expiry
        if (isExpired) {
            println("⚠️ Token has expired")
        }
        return isExpired
    }

    fun isTokenExpiringSoon(thresholdMinutes: Int = 5): Boolean {
        val expiry = getTokenExpiry()
        if (expiry == 0L) return true

        val thresholdMillis = thresholdMinutes * 60 * 1000L
        val timeUntilExpiry = expiry - System.currentTimeMillis()

        val isExpiringSoon = timeUntilExpiry <= thresholdMillis

        if (isExpiringSoon && timeUntilExpiry > 0) {
            val minutesLeft = timeUntilExpiry / 1000 / 60
            println("⚠️ Token expires in $minutesLeft minutes")
        }

        return isExpiringSoon
    }

    fun getTimeUntilExpiry(): Long {
        val expiry = getTokenExpiry()
        if (expiry == 0L) return 0

        val timeRemaining = (expiry - System.currentTimeMillis()) / 1000
        return maxOf(0, timeRemaining)
    }

    fun printExpiryInfo() {
        val expiry = getTokenExpiry()
        if (expiry == 0L) {
            println("⚠️ No token expiry information available")
            return
        }

        val now = System.currentTimeMillis()
        val timeRemaining = expiry - now

        if (timeRemaining > 0) {
            val minutes = timeRemaining / 1000 / 60
            val seconds = (timeRemaining / 1000) % 60
            println("⏱️ Token expires in: ${minutes}m ${seconds}s")
        } else {
            val expiredAgo = -timeRemaining / 1000 / 60
            println("❌ Token expired $expiredAgo minutes ago")
        }
    }

    fun hasValidSession(): Boolean {
        val hasIdToken = getIdToken() != null
        val hasRefreshToken = getRefreshToken() != null
        val notExpired = !isTokenExpired()

        val isValid = hasIdToken && hasRefreshToken && notExpired
        println("🔍 Session check: hasIdToken=$hasIdToken, hasRefreshToken=$hasRefreshToken, notExpired=$notExpired")
        return isValid
    }

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

    fun clearAll() {
        prefs.clear()
        prefs.flush()
        println("🗑️ Cleared all stored tokens")
    }

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

data class StoredUser(
    val uid: String,
    val email: String,
    val idToken: String
)
