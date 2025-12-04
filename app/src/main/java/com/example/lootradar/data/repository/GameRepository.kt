package com.example.lootradar.data.repository

import com.example.lootradar.data.models.GameDto
import com.example.lootradar.data.remote.ApiService

class GameRepository(
    private val api: ApiService
) {
    suspend fun getFreeGames(): List<GameDto> {
        return api.getFreeGames()
    }
}