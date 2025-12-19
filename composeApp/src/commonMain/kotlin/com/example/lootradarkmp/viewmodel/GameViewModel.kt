package com.example.lootradarkmp.viewmodel

import com.example.lootradarkmp.data.GameDatabaseProvider
import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.remote.ApiService
import com.example.lootradarkmp.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GameViewModel {

    private val repository: GameRepository = GameRepository(ApiService())

    private val _games = MutableStateFlow<List<GameDto>>(emptyList())
    val games = _games.asStateFlow()

    init {
        loadGames()
    }

    private fun loadGames() {
        CoroutineScope(Dispatchers.Main).launch {
            repository.getFreeGames()
                .catch { e ->
                    println("Error: $e")
                }
                .collect { gameList ->
                    _games.value = gameList
                }
        }
    }
    suspend fun debugDbTest() {
        val database = GameDatabaseProvider.getDatabase()
        database.gameQueries.insertGame(
            id = 999L,
            title = "DB Test Game",
            worth = "$19.99",
            thumbnail = null,
            image = null,
            description = "Inserted manually",
            instructions = null,
            open_giveaway_url = null,
            published_date = null,
            type = "Game",
            platforms = "PC",
            end_date = null,
            users = 0L,
            status = "Active",
            gamerpower_url = null
        )

        val games = database.gameQueries.selectAll().executeAsList()
        println("DB CONTENTS: $games")
    }
}