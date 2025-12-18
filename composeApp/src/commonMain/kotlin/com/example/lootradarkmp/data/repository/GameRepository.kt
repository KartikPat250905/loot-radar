package com.example.lootradarkmp.data.repository

import com.example.lootradarkmp.data.GameDatabaseProvider
import com.example.lootradarkmp.data.mappers.toDto
import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.remote.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class GameRepository(
    private val api: ApiService
) {

    private val database = GameDatabaseProvider.getDatabase()

    fun getFreeGames(): Flow<List<GameDto>> = flow {
        val cached = database.gameQueries.selectAll().executeAsList().map { it.toDto() }
        if (cached.isNotEmpty()) emit(cached)

        try {
            api.getFreeGamesFlow().collect { remoteGames ->
                database.transaction {
                    database.gameQueries.deleteAll()
                    remoteGames.forEach { game ->
                        database.gameQueries.insertGame(
                            id = game.id?.toLong() ?: return@forEach,
                            title = game.title,
                            worth = game.worth,
                            thumbnail = game.thumbnail,
                            image = game.image,
                            description = game.description,
                            instructions = game.instructions,
                            open_giveaway_url = game.open_giveaway_url,
                            published_date = game.published_date,
                            type = game.type,
                            platforms = game.platforms,
                            end_date = game.end_date,
                            users = game.users?.toLong(),
                            status = game.status,
                            gamerpower_url = game.gamerpower_url
                        )
                    }
                }

                emit(remoteGames)
            }
        } catch (e: Exception) {
            // API failed, just continue using cached DB data
            println("API failed, returning cached DB: ${e.message}")
        }
    }

}
