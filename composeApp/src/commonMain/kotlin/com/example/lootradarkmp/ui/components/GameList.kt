package com.example.lootradarkmp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.repository.GameRepository

@Composable
fun GameList() {

    LaunchedEffect(Unit)
    {
        val repository = GameRepository()

        try {
            val games: List<GameDto> = repository.getFreeGames()
            println("Fetched ${games.size} games")
            games.forEach { println(it) }  // Print full DTO for testing
        } catch (e: Exception) {
            println("Error fetching games: $e")
        }
    }
}
