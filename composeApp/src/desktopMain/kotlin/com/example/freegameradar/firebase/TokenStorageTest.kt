package com.example.freegameradar.firebase

import kotlinx.coroutines.delay

/**
 * Test token storage functionality
 */

/**
 * Test 1: Basic storage and retrieval
 */
fun testBasicStorageRetrieval() {
    println("\n=== Test 1: Basic Storage & Retrieval ===")

    // Clear any existing data
    TokenStorage.clearAll()

    // Save test data
    TokenStorage.saveIdToken("test_id_token_123")
    TokenStorage.saveRefreshToken("test_refresh_token_456")
    TokenStorage.saveUserEmail("test@example.com")
    TokenStorage.saveUserId("user_789")

    // Retrieve and verify
    val idToken = TokenStorage.getIdToken()
    val refreshToken = TokenStorage.getRefreshToken()
    val email = TokenStorage.getUserEmail()
    val userId = TokenStorage.getUserId()

    println("Retrieved ID Token: ${idToken?.take(20)}...")
    println("Retrieved Refresh Token: ${refreshToken?.take(20)}...")
    println("Retrieved Email: $email")
    println("Retrieved User ID: $userId")

    if (idToken == "test_id_token_123" &&
        refreshToken == "test_refresh_token_456" &&
        email == "test@example.com" &&
        userId == "user_789") {
        println("✅ Basic storage and retrieval working!")
    } else {
        println("❌ Storage/retrieval test failed")
    }
}

/**
 * Test 2: Token expiry calculation
 */
fun testTokenExpiry() {
    println("\n=== Test 2: Token Expiry ===")

    // Save a token that expires in 5 seconds
    TokenStorage.saveTokenExpiry("5")

    println("Token expires in 5 seconds...")
    println("Is expired now? ${TokenStorage.isTokenExpired()}")

    if (!TokenStorage.isTokenExpired()) {
        println("✅ Token correctly marked as not expired")
    } else {
        println("❌ Token incorrectly marked as expired")
    }
}

/**
 * Test 3: Session validity check
 */
fun testSessionValidity() {
    println("\n=== Test 3: Session Validity ===")

    // Clear and set up valid session
    TokenStorage.clearAll()
    TokenStorage.saveIdToken("valid_token")
    TokenStorage.saveRefreshToken("valid_refresh")
    TokenStorage.saveTokenExpiry("3600") // 1 hour

    println("Has valid session? ${TokenStorage.hasValidSession()}")

    if (TokenStorage.hasValidSession()) {
        println("✅ Valid session correctly detected")
    } else {
        println("❌ Valid session not detected")
    }

    // Test invalid session
    TokenStorage.clearAll()
    println("After clearing - Has valid session? ${TokenStorage.hasValidSession()}")

    if (!TokenStorage.hasValidSession()) {
        println("✅ Invalid session correctly detected")
    } else {
        println("❌ Invalid session incorrectly marked as valid")
    }
}

/**
 * Test 4: Complete auth response storage
 */
fun testAuthResponseStorage() {
    println("\n=== Test 4: Complete Auth Response Storage ===")

    val testResponse = FirebaseAuthResponse(
        idToken = "complete_test_token",
        email = "complete@example.com",
        refreshToken = "complete_refresh_token",
        expiresIn = "3600",
        localId = "complete_user_id"
    )

    TokenStorage.saveAuthResponse(testResponse)

    val storedUser = TokenStorage.getStoredUser()

    if (storedUser != null) {
        println("✅ Complete auth response stored successfully")
        println("   - User ID: ${storedUser.uid}")
        println("   - Email: ${storedUser.email}")
        println("   - Has token: ${storedUser.idToken.isNotEmpty()}")
    } else {
        println("❌ Failed to store complete auth response")
    }
}

/**
 * Test 5: Persistence test (simulates app restart)
 */
fun testPersistence() {
    println("\n=== Test 5: Persistence Test ===")

    // Save data
    TokenStorage.clearAll()
    TokenStorage.saveIdToken("persistent_token")
    TokenStorage.saveUserEmail("persistent@example.com")

    println("Saved data...")
    println("Simulating app restart (just re-reading from storage)...")

    // Read back (Java Preferences persists automatically)
    val token = TokenStorage.getIdToken()
    val email = TokenStorage.getUserEmail()

    if (token == "persistent_token" && email == "persistent@example.com") {
        println("✅ Data persists correctly!")
        println("   Note: To truly test persistence, restart the app")
    } else {
        println("❌ Persistence test failed")
    }
}

/**
 * Test 6: Integration with actual sign-in
 */
suspend fun testSignInWithStorage() {
    println("\n=== Test 6: Sign In with Token Storage ===")

    val authService = FirebaseAuthService()

    // Clear existing session
    TokenStorage.clearAll()

    println("Before sign in:")
    TokenStorage.printStorageState()

    // Sign in (use your test account)
    val testEmail = "test@example.com"
    val testPassword = "testpassword123"

    val result = authService.signIn(testEmail, testPassword)

    result.onSuccess {
        println("\nAfter successful sign in:")
        TokenStorage.printStorageState()

        // Check if we can retrieve current user
        val currentUser = authService.getCurrentUser()
        if (currentUser != null) {
            println("✅ Current user retrieved from storage:")
            println("   - Email: ${currentUser.email}")
            println("   - User ID: ${currentUser.uid}")
        }
    }

    result.onFailure {
        println("❌ Sign in failed, cannot test storage integration")
    }
}

/**
 * Run all token storage tests
 */
suspend fun runAllTokenStorageTests() {
    println("\n" + "=".repeat(50))
    println("💾 TOKEN STORAGE TESTS (Desktop Only)")
    println("=".repeat(50))

    testBasicStorageRetrieval()
    delay(500)

    testTokenExpiry()
    delay(500)

    testSessionValidity()
    delay(500)

    testAuthResponseStorage()
    delay(500)

    testPersistence()
    delay(500)

    testSignInWithStorage()

    println("\n" + "=".repeat(50))
    println("✅ All Token Storage Tests Complete")
    println("=".repeat(50) + "\n")
}
