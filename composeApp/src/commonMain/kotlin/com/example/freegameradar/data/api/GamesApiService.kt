package com.example.freegameradar.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GameApiResponse(
    val id: Long,
    val title: String,
    val worth: String,
    val thumbnail: String,
    val image: String,
    val description: String,
    val instructions: String? = null,
    val open_giveaway_url: String,
    val published_date: String,
    val type: String,
    val platforms: String,
    val end_date: String,
    val users: Int,
    val status: String,
    val gamerpower_url: String
)

class GamesApiService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun fetchAllGames(): Result<List<GameApiResponse>> {
        return try {
            println("🌐 Fetching games from API...")
            val games: List<GameApiResponse> = client
                .get("https://www.gamerpower.com/api/giveaways")
                .body()
            println("✅ API returned ${games.size} games")
            Result.success(games)
        } catch (e: Exception) {
            println("❌ API fetch failed: ${e.message}")
            Result.failure(e)
        }
    }
}