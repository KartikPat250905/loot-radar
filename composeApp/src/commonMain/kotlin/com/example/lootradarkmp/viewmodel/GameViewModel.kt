package com.example.lootradarkmp.viewmodel

import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.repository.GameRepository

class GameViewModel(val repository: GameRepository)
{
    var games : List<GameDto> = emptyList()
        private set

    suspend fun loadGames()
    {
        try {
            repository.getFreeGames()
        }
        catch (e: Exception){
            println("Error: $e")
        }
    }
}