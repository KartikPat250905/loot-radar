package com.example.freegameradar.firebase

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * HTTP client specifically for Firebase REST API calls on Desktop
 * Android uses native Firebase SDK, not this client
 */
object FirebaseHttpClient {
    
    val client = HttpClient(OkHttp) {
        // JSON content negotiation
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true  // Ignore extra fields from Firebase
                isLenient = true          // Accept lenient JSON
                prettyPrint = true        // Pretty print for debugging
                encodeDefaults = true     // Include default values
            })
        }
        
        // Logging for debugging (disable in production)
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("Firebase HTTP: $message")
                }
            }
            level = LogLevel.INFO  // Change to LogLevel.BODY for full request/response logging
        }
        
        // Engine configuration
        engine {
            config {
                followRedirects(true)
            }
        }
    }
    
    /**
     * Clean up resources when app closes
     */
    fun close() {
        client.close()
    }
}