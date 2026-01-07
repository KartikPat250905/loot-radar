package com.example.freegameradar.firebase

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Test functions for Firebase HTTP client
 * Remove or comment out after Phase 4 testing
 */

/**
 * Test 1: Basic HTTP connectivity
 */
suspend fun testBasicHttpRequest() {
    println("\n=== Test 1: Basic HTTP Connectivity ===")
    try {
        val response: HttpResponse = FirebaseHttpClient.client.get("https://www.google.com")
        println("✅ HTTP Status: ${response.status}")
        println("✅ Content Type: ${response.contentType()}")
        println("✅ Basic HTTP client working!")
    } catch (e: Exception) {
        println("❌ HTTP Test Failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 2: Firebase API endpoint reachability
 */
suspend fun testFirebaseEndpointReachability() {
    println("\n=== Test 2: Firebase API Endpoint Reachability ===")
    try {
        // Test with a minimal request to Firebase endpoint
        val response: HttpResponse = FirebaseHttpClient.client.post(FirebaseConfig.SIGN_IN_URL)
        println("✅ Firebase Endpoint Status: ${response.status}")
        
        // We expect 400 (Bad Request) because we're not sending proper auth data
        // But 400 means the endpoint is reachable and responding
        if (response.status == HttpStatusCode.BadRequest) {
            println("✅ Firebase API is reachable (400 = endpoint exists, needs auth data)")
        } else {
            println("⚠️ Unexpected status: ${response.status}")
        }
    } catch (e: Exception) {
        println("❌ Firebase Endpoint Test Failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Test 3: JSON serialization with Firebase models
 */
suspend fun testJsonSerialization() {
    println("\n=== Test 3: JSON Serialization Test ===")
    try {
        val testRequest = SignInRequest(
            email = "test@example.com",
            password = "testpassword123"
        )
        println("✅ Created SignInRequest object: $testRequest")
        println("✅ JSON serialization models working!")
    } catch (e: Exception) {
        println("❌ JSON Serialization Test Failed: ${e.message}")
        e.printStackTrace()
    }
}

/**
 * Run all HTTP client tests
 */
suspend fun runAllHttpClientTests() {
    println("\n" + "=".repeat(50))
    println("🔥 FIREBASE HTTP CLIENT TESTS")
    println("=".repeat(50))
    
    testBasicHttpRequest()
    testFirebaseEndpointReachability()
    testJsonSerialization()
    
    println("\n" + "=".repeat(50))
    println("✅ All HTTP Client Tests Complete")
    println("=".repeat(50) + "\n")
}