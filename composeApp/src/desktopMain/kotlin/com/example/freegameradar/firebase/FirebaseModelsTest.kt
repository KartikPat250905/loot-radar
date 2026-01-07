package com.example.freegameradar.firebase

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Test serialization/deserialization of Firebase models
 * Desktop only - Android uses native Firebase SDK
 */

private val json = Json {
    prettyPrint = true
    ignoreUnknownKeys = true
}

/**
 * Test 1: Sign In Request Serialization
 */
fun testSignInRequestSerialization() {
    println("\n=== Test 1: Sign In Request Serialization ===")
    try {
        val request = SignInRequest(
            email = "test@example.com",
            password = "password123"
        )

        val jsonString = json.encodeToString(request)
        println("✅ Serialized SignInRequest:")
        println(jsonString)

        val deserialized = json.decodeFromString<SignInRequest>(jsonString)
        println("✅ Deserialized back: $deserialized")
        println("✅ Sign In Request model working!")
    } catch (e: Exception) {
        println("❌ Sign In Request test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 2: Sign Up Request Serialization
 */
fun testSignUpRequestSerialization() {
    println("\n=== Test 2: Sign Up Request Serialization ===")
    try {
        val request = SignUpRequest(
            email = "newuser@example.com",
            password = "securepass456"
        )

        val jsonString = json.encodeToString(request)
        println("✅ Serialized SignUpRequest:")
        println(jsonString)
        println("✅ Sign Up Request model working!")
    } catch (e: Exception) {
        println("❌ Sign Up Request test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 3: Auth Response Deserialization
 */
fun testAuthResponseDeserialization() {
    println("\n=== Test 3: Auth Response Deserialization ===")
    try {
        // Sample Firebase auth response JSON
        val sampleResponse = """
        {
            "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjEyMzQ1...",
            "email": "test@example.com",
            "refreshToken": "AEu4IL2L3xK7...",
            "expiresIn": "3600",
            "localId": "abc123def456",
            "registered": true
        }
        """.trimIndent()

        val response = json.decodeFromString<FirebaseAuthResponse>(sampleResponse)
        println("✅ Deserialized FirebaseAuthResponse:")
        println("   - Email: ${response.email}")
        println("   - LocalId: ${response.localId}")
        println("   - ExpiresIn: ${response.expiresIn} seconds")
        println("✅ Auth Response model working!")
    } catch (e: Exception) {
        println("❌ Auth Response test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 4: Error Response Deserialization
 */
fun testErrorResponseDeserialization() {
    println("\n=== Test 4: Error Response Deserialization ===")
    try {
        // Sample Firebase error response
        val sampleError = """
        {
            "error": {
                "code": 400,
                "message": "EMAIL_NOT_FOUND",
                "errors": [
                    {
                        "message": "EMAIL_NOT_FOUND",
                        "domain": "global",
                        "reason": "invalid"
                    }
                ]
            }
        }
        """.trimIndent()

        val errorResponse = json.decodeFromString<FirebaseErrorResponse>(sampleError)
        println("✅ Deserialized FirebaseErrorResponse:")
        println("   - Code: ${errorResponse.error.code}")
        println("   - Message: ${errorResponse.error.message}")
        println("✅ Error Response model working!")
    } catch (e: Exception) {
        println("❌ Error Response test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 5: Password Reset Request Serialization
 */
fun testPasswordResetRequestSerialization() {
    println("\n=== Test 5: Password Reset Request Serialization ===")
    try {
        val request = PasswordResetRequest(
            email = "forgot@example.com"
        )

        val jsonString = json.encodeToString(request)
        println("✅ Serialized PasswordResetRequest:")
        println(jsonString)
        println("✅ Password Reset Request model working!")
    } catch (e: Exception) {
        println("❌ Password Reset Request test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 6: Refresh Token Request/Response
 */
fun testRefreshTokenModels() {
    println("\n=== Test 6: Refresh Token Models ===")
    try {
        // Request
        val request = RefreshTokenRequest(
            refreshToken = "AEu4IL2L3xK7..."
        )
        val requestJson = json.encodeToString(request)
        println("✅ Serialized RefreshTokenRequest:")
        println(requestJson)

        // Response
        val sampleResponse = """
        {
            "id_token": "newIdToken123",
            "refresh_token": "newRefreshToken456",
            "expires_in": "3600",
            "user_id": "abc123"
        }
        """.trimIndent()

        val response = json.decodeFromString<RefreshTokenResponse>(sampleResponse)
        println("✅ Deserialized RefreshTokenResponse:")
        println("   - UserId: ${response.userId}")
        println("   - ExpiresIn: ${response.expiresIn} seconds")
        println("✅ Refresh Token models working!")
    } catch (e: Exception) {
        println("❌ Refresh Token models test failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Run all model tests
 */
fun runAllModelTests() {
    println("\n" + "=".repeat(50))
    println("📦 FIREBASE DATA MODELS TESTS (Desktop Only)")
    println("=".repeat(50))

    testSignInRequestSerialization()
    testSignUpRequestSerialization()
    testAuthResponseDeserialization()
    testErrorResponseDeserialization()
    testPasswordResetRequestSerialization()
    testRefreshTokenModels()

    println("\n" + "=".repeat(50))
    println("✅ All Data Model Tests Complete")
    println("=".repeat(50) + "\n")
}
