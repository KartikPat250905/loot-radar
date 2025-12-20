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
    fun getFreeGamesFlow(): Flow<List<GameDto>> = flow {
        try {
            val response = client.get("https://www.gamerpower.com/api/giveaways") {
                contentType(ContentType.Application.Json)
            }

            val games: List<GameDto> = response.body()

            emit(games)
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            throw e
        }
    }
}
