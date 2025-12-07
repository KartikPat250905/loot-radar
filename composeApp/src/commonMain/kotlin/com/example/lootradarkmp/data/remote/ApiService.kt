package com.example.lootradarkmp.data.remote

import com.example.lootradarkmp.data.models.GameDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ApiService {
    private val client = NetworkClient.client

    // We use a Flow here to "emit" (send) data over time.
    // In this simple case, we just emit one list of games once we get it from the API.
    // Think of it like a pipe: we get data from the internet, and push it into the pipe.
    fun getFreeGamesFlow(): Flow<List<GameDto>> = flow {
        try {
            // 1. Make the network request to the URL
            val response = client.get("https://www.gamerpower.com/api/giveaways") {
                contentType(ContentType.Application.Json) // We expect JSON back
            }
            
            // 2. Convert the response JSON into our list of GameDto objects
            val games: List<GameDto> = response.body()
            
            // 3. "Emit" the list. This sends the data to anyone listening to this Flow.
            emit(games)
        } catch (e: Exception) {
            // If something goes wrong, print it and crash (for now)
            println("API Error: ${e.message}")
            throw e
        }
    }
}
