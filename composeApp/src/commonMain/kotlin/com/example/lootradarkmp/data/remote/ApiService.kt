package com.example.lootradarkmp.data.remote

import com.example.lootradarkmp.data.models.GameDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType

class ApiService {
    private val client = NetworkClient.client

    suspend fun getFreeGames(): List<GameDto> {
        try {
            val response = client.get("https://www.gamerpower.com/api/giveaways") {
                contentType(ContentType.Application.Json)
            }
            return response.body()
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            throw e
        }
    }
}