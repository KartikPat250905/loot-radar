package com.example.lootradarkmp.data.repository

import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.remote.ApiService

class GameRepository(
    private val api: ApiService = ApiService()
) {
    suspend fun getFreeGames(): List<GameDto> {
        return api.getFreeGames()
    }
}
