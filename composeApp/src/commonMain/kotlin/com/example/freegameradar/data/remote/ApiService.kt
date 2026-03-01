package com.radarlabs.freegameradar.data.remote

import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.data.models.WorthDto
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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
            emit(response.body<List<GameDto>>())
        } catch (e: Exception) {
            println("API Error: ${e.message}")
            throw e
        }
    }

    suspend fun getTotalWorth(): WorthDto {
        return client.get("https://www.gamerpower.com/api/worth") {
            contentType(ContentType.Application.Json)
        }.body()
    }

    suspend fun getGameById(id: String): GameDto {
        return client.get("https://www.gamerpower.com/api/giveaway") {
            parameter("id", id)
            contentType(ContentType.Application.Json)
        }.body()
    }
}
