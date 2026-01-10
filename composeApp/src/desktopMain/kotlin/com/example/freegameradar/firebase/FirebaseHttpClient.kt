package com.example.freegameradar.firebase

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object FirebaseHttpClient {
    
    val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) {
                    println("Firebase HTTP: $message")
                }
            }
            level = LogLevel.INFO
        }

        engine {
            config {
                followRedirects(true)
            }
        }
    }

    fun close() {
        client.close()
    }
}