package com.example.lootradarkmp.data.repository

import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.remote.ApiService
import kotlinx.coroutines.flow.Flow

// The Repository acts as a "middleman" between the data source (API) and the screen (UI).
// It decides where to get data from. Right now, it just asks the ApiService.
class GameRepository(
    private val api: ApiService = ApiService()
) {
    // This function just passes the Flow from the API to whoever calls it (the ViewModel or UI).
    fun getFreeGames(): Flow<List<GameDto>> {
        return api.getFreeGamesFlow()
    }
}
