package com.example.freegameradar.firebase

/**
 * Firebase configuration for REST API authentication
 * Used by desktop target via Firebase Auth REST API
 */
object FirebaseConfig {
    // Your Firebase Web SDK Configuration
    const val API_KEY = "AIzaSyBYstqCjksC4l4smftt36hCP6jB_D2FBXg"
    const val PROJECT_ID = "lootradar-kmp"
    const val AUTH_DOMAIN = "lootradar-kmp.firebaseapp.com"
    const val DATABASE_URL = "https://lootradar-kmp-default-rtdb.firebaseio.com"
    const val STORAGE_BUCKET = "lootradar-kmp.firebasestorage.app"
    const val MESSAGING_SENDER_ID = "1028502883068"
    const val APP_ID = "1:1028502883068:web:3bd43ad0b0a43147306579"
    
    // Firebase Auth REST API Endpoints
    private const val IDENTITY_TOOLKIT_BASE = "https://identitytoolkit.googleapis.com/v1"
    private const val SECURE_TOKEN_BASE = "https://securetoken.googleapis.com/v1"
    
    // Authentication endpoints
    const val SIGN_UP_URL = "$IDENTITY_TOOLKIT_BASE/accounts:signUp?key=$API_KEY"
    const val SIGN_IN_URL = "$IDENTITY_TOOLKIT_BASE/accounts:signInWithPassword?key=$API_KEY"
    const val PASSWORD_RESET_URL = "$IDENTITY_TOOLKIT_BASE/accounts:sendOobCode?key=$API_KEY"
    const val REFRESH_TOKEN_URL = "$SECURE_TOKEN_BASE/token?key=$API_KEY"
    const val GET_USER_DATA_URL = "$IDENTITY_TOOLKIT_BASE/accounts:lookup?key=$API_KEY"
    const val DELETE_ACCOUNT_URL = "$IDENTITY_TOOLKIT_BASE/accounts:delete?key=$API_KEY"
    const val UPDATE_PROFILE_URL = "$IDENTITY_TOOLKIT_BASE/accounts:update?key=$API_KEY"
    
    // Token refresh grant type
    const val REFRESH_GRANT_TYPE = "refresh_token"
}