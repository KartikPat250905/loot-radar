package com.example.freegameradar.firebase

import kotlinx.coroutines.delay

/**
 * Test Firebase Authentication Service (Desktop Only)
 * These tests will make real API calls to Firebase
 */

private val authService = FirebaseAuthService()

/**
 * Test 1: Sign Up with new account
 * Note: Use a unique email each time or this will fail with EMAIL_EXISTS
 */
suspend fun testSignUp() {
    println("\n=== Test 1: Sign Up ===")

    // Generate unique email for testing
    val timestamp = System.currentTimeMillis()
    val testEmail = "testuser_${timestamp}@example.com"
    val testPassword = "TestPassword123!"

    println("Creating account: $testEmail")

    val result = authService.signUp(testEmail, testPassword)

    result.onSuccess { response ->
        println("✅ Sign Up Success!")
        println("   - User ID: ${response.localId}")
        println("   - Email: ${response.email}")
        println("   - Token expires in: ${response.expiresIn} seconds")
    }

    result.onFailure { error ->
        println("❌ Sign Up Failed: ${error.message}")
        if (error.message?.contains("EMAIL_EXISTS") == true) {
            println("   (This is expected if email already exists)")
        }
    }
}

/**
 * Test 2: Sign In with existing account
 * Note: You need to manually create this account in Firebase Console first,
 * or use the account created in Test 1
 */
suspend fun testSignIn() {
    println("\n=== Test 2: Sign In ===")

    // Use test credentials - UPDATE THESE to match your Firebase test account
    val testEmail = "test@example.com"
    val testPassword = "testpassword123"

    println("Signing in: $testEmail")

    val result = authService.signIn(testEmail, testPassword)

    result.onSuccess { response ->
        println("✅ Sign In Success!")
        println("   - User ID: ${response.localId}")
        println("   - Email: ${response.email}")
        println("   - Has ID Token: ${response.idToken.isNotEmpty()}")
        println("   - Has Refresh Token: ${response.refreshToken.isNotEmpty()}")
    }

    result.onFailure { error ->
        println("❌ Sign In Failed: ${error.message}")
        println("   Note: Create a test account in Firebase Console with:")
        println("   Email: test@example.com")
        println("   Password: testpassword123")
    }
}

/**
 * Test 3: Sign In with wrong password
 */
suspend fun testSignInWrongPassword() {
    println("\n=== Test 3: Sign In with Wrong Password ===")

    val testEmail = "test@example.com"
    val wrongPassword = "wrongpassword"

    val result = authService.signIn(testEmail, wrongPassword)

    result.onSuccess {
        println("⚠️ Unexpected success with wrong password!")
    }

    result.onFailure { error ->
        println("✅ Correctly rejected wrong password")
        println("   Error: ${error.message}")
    }
}

/**
 * Test 4: Sign In with non-existent email
 */
suspend fun testSignInNonExistentEmail() {
    println("\n=== Test 4: Sign In with Non-Existent Email ===")

    val nonExistentEmail = "nonexistent_${System.currentTimeMillis()}@example.com"
    val testPassword = "somepassword"

    val result = authService.signIn(nonExistentEmail, testPassword)

    result.onSuccess {
        println("⚠️ Unexpected success with non-existent email!")
    }

    result.onFailure { error ->
        println("✅ Correctly rejected non-existent email")
        println("   Error: ${error.message}")
    }
}

/**
 * Test 5: Password Reset Email
 */
suspend fun testPasswordResetEmail() {
    println("\n=== Test 5: Password Reset Email ===")

    val testEmail = "test@example.com"

    val result = authService.sendPasswordResetEmail(testEmail)

    result.onSuccess {
        println("✅ Password reset email sent successfully!")
        println("   Check the inbox for: $testEmail")
    }

    result.onFailure { error ->
        println("❌ Password reset failed: ${error.message}")
    }
}

/**
 * Test 6: Complete authentication flow
 */
suspend fun testCompleteAuthFlow() {
    println("\n=== Test 6: Complete Authentication Flow ===")

    // Step 1: Sign Up
    val timestamp = System.currentTimeMillis()
    val email = "flowtest_${timestamp}@example.com"
    val password = "FlowTest123!"

    println("Step 1: Creating new account...")
    val signUpResult = authService.signUp(email, password)

    if (signUpResult.isFailure) {
        println("❌ Flow test failed at sign up")
        return
    }

    val signUpResponse = signUpResult.getOrNull()!!
    println("✅ Account created: ${signUpResponse.localId}")

    delay(1000) // Wait a bit between requests

    // Step 2: Sign In with same credentials
    println("\nStep 2: Signing in with new account...")
    val signInResult = authService.signIn(email, password)

    if (signInResult.isFailure) {
        println("❌ Flow test failed at sign in")
        return
    }

    val signInResponse = signInResult.getOrNull()!!
    println("✅ Signed in successfully")

    delay(1000)

    // Step 3: Delete the test account
    println("\nStep 3: Cleaning up - deleting test account...")
    val deleteResult = authService.deleteAccount(signInResponse.idToken)

    deleteResult.onSuccess {
        println("✅ Test account deleted")
    }

    deleteResult.onFailure {
        println("⚠️ Could not delete test account: ${it.message}")
    }

    println("\n✅ Complete authentication flow test finished!")
}

/**
 * Test 7: Sign-Up Flow
 */
suspend fun testSignUpFlow() {
    println("\n=== Test 7: Sign-Up Flow ===")

    val timestamp = System.currentTimeMillis()
    val testEmail = "signuptest_${timestamp}@example.com"
    val testPassword = "TestPass123!"

    println("Creating new account: $testEmail")

    val result = authService.signUp(testEmail, testPassword)

    result.onSuccess { response ->
        println("✅ Sign-Up Success!")
        println("   - User ID: ${response.localId}")
        println("   - Email: ${response.email}")
        println("   - Tokens saved to storage")

        TokenStorage.printStorageState()

        // Clean up: delete the test account
        delay(1000)
        println("\nCleaning up test account...")
        val deleteResult = authService.deleteAccount(response.idToken)
        deleteResult.onSuccess {
            println("✅ Test account deleted")
        }
    }

    result.onFailure { error ->
        println("❌ Sign-Up Failed: ${error.message}")
        println("   (Expected errors: EMAIL_EXISTS if already used)")
    }
}

/**
 * Run all authentication service tests
 */
suspend fun runAllAuthServiceTests() {
    println("\n" + "=".repeat(50))
    println("🔐 FIREBASE AUTH SERVICE TESTS (Desktop Only)")
    println("=".repeat(50))
    println("\n⚠️  These tests make REAL API calls to Firebase!")
    println("⚠️  Make sure you have a test account in Firebase Console")

    delay(1000)

    // Basic tests
    testSignInNonExistentEmail()
    delay(1000)

    testSignInWrongPassword()
    delay(1000)

    testSignIn()
    delay(1000)

    // Add to the test sequence:
    testSignUpFlow()
    delay(1000)

    // Only run these if you want to test account creation
    // testSignUp()
    // delay(1000)

    // testPasswordResetEmail()
    // delay(1000)

    // Complete flow test (creates and deletes test account)
    // testCompleteAuthFlow()

    println("\n" + "=".repeat(50))
    println("✅ All Auth Service Tests Complete")
    println("=".repeat(50) + "\n")
}
