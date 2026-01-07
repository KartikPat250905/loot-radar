package com.example.freegameradar.firebase

import kotlinx.coroutines.delay

/**
 * Test token refresh functionality
 */

private val authService = FirebaseAuthService()

/**
 * Test 1: Check token expiry detection
 */
fun testTokenExpiryDetection() {
    println("\n=== Test 1: Token Expiry Detection ===")
    
    // Set token that expires in 1 minute
    TokenStorage.saveTokenExpiry("60")
    
    println("Token expires in 60 seconds")
    println("Is expired? ${TokenStorage.isTokenExpired()}")
    println("Is expiring soon? ${TokenStorage.isTokenExpiringSoon()}")
    
    TokenStorage.printExpiryInfo()
    
    // Set already expired token
    TokenStorage.saveTokenExpiry("0")
    println("\nAfter setting expired token:")
    println("Is expired? ${TokenStorage.isTokenExpired()}")
    
    if (TokenStorage.isTokenExpired()) {
        println("✅ Expired token correctly detected")
    }
}

/**
 * Test 2: Manual token refresh
 */
suspend fun testManualTokenRefresh() {
    println("\n=== Test 2: Manual Token Refresh ===")
    
    // First, sign in to get tokens
    println("Step 1: Signing in to get initial tokens...")
    val signInResult = authService.signIn("test@example.com", "testpassword123")
    
    if (signInResult.isFailure) {
        println("❌ Cannot test refresh - sign in failed")
        return
    }
    
    println("✅ Signed in successfully")
    TokenStorage.printExpiryInfo()
    
    delay(2000)
    
    // Now try to refresh
    println("\nStep 2: Manually refreshing token...")
    val refreshResult = authService.refreshToken()
    
    refreshResult.onSuccess { response ->
        println("✅ Token refresh successful!")
        println("   - User ID: ${response.userId}")
        println("   - Expires in: ${response.expiresIn} seconds")
        TokenStorage.printExpiryInfo()
    }
    
    refreshResult.onFailure { error ->
        println("❌ Token refresh failed: ${error.message}")
    }
}

/**
 * Test 3: Auto-refresh when token expired
 */
suspend fun testAutoRefresh() {
    println("\n=== Test 3: Auto-Refresh Logic ===")
    
    // Sign in first
    val signInResult = authService.signIn("test@example.com", "testpassword123")
    
    if (signInResult.isFailure) {
        println("❌ Cannot test auto-refresh - sign in failed")
        return
    }
    
    println("✅ Signed in")
    
    // Manually mark token as expired to trigger auto-refresh
    println("\nSimulating expired token...")
    TokenStorage.saveTokenExpiry("0")
    
    println("Is logged in with valid token? ${authService.isLoggedInWithValidToken()}")
    
    // This should auto-refresh
    val isValid = authService.isLoggedInWithValidToken()
    
    if (isValid) {
        println("✅ Auto-refresh successful!")
        TokenStorage.printExpiryInfo()
    } else {
        println("❌ Auto-refresh failed")
    }
}

/**
 * Test 4: Refresh if needed (proactive refresh)
 */
suspend fun testRefreshIfNeeded() {
    println("\n=== Test 4: Proactive Refresh ===")
    
    // Sign in
    val signInResult = authService.signIn("test@example.com", "testpassword123")
    
    if (signInResult.isFailure) {
        println("❌ Cannot test - sign in failed")
        return
    }
    
    println("✅ Signed in with fresh token")
    TokenStorage.printExpiryInfo()
    
    // Check if refresh is needed (should be false)
    println("\nChecking if refresh needed...")
    val result1 = authService.refreshTokenIfNeeded()
    result1.onSuccess { wasRefreshed ->
        if (!wasRefreshed) {
            println("✅ No refresh needed - token still valid")
        }
    }
    
    // Now simulate token expiring soon
    println("\nSimulating token expiring in 2 minutes...")
    TokenStorage.saveTokenExpiry("120")  // 2 minutes
    
    val result2 = authService.refreshTokenIfNeeded()
    result2.onSuccess { wasRefreshed ->
        if (wasRefreshed) {
            println("✅ Proactive refresh successful!")
            TokenStorage.printExpiryInfo()
        }
    }
}

/**
 * Run all token refresh tests
 */
suspend fun runAllTokenRefreshTests() {
    println("\n" + "=".repeat(50))
    println("🔄 TOKEN REFRESH TESTS (Desktop Only)")
    println("=".repeat(50))
    
    testTokenExpiryDetection()
    delay(1000)
    
    testManualTokenRefresh()
    delay(1000)
    
    testAutoRefresh()
    delay(1000)
    
    testRefreshIfNeeded()
    
    println("\n" + "=".repeat(50))
    println("✅ All Token Refresh Tests Complete")
    println("=".repeat(50) + "\n")
}