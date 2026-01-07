package com.example.freegameradar.firebase

/**
 * Simple verification that Firebase config is accessible
 * Remove this file after Phase 3 testing
 */
fun testFirebaseConfig() {
    println("=== Firebase Configuration Test ===")
    println("Project ID: ${FirebaseConfig.PROJECT_ID}")
    println("Auth Domain: ${FirebaseConfig.AUTH_DOMAIN}")
    println("Sign In URL: ${FirebaseConfig.SIGN_IN_URL}")
    println("Sign Up URL: ${FirebaseConfig.SIGN_UP_URL}")
    println("Config loaded successfully ✅")
}