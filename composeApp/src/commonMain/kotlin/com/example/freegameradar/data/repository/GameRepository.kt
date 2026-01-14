package com.example.freegameradar.data.repository

import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.mappers.toDto
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.remote.ApiService
import com.example.freegameradar.data.state.DataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow


class GameRepository(
    private val api: ApiService
) {
    private val _dataSource = MutableStateFlow(DataSource.NETWORK)
    val dataSource: StateFlow<DataSource> = _dataSource
    private val database = GameDatabaseProvider.getDatabase()
    private val notificationRepository = NotificationRepository(database)

    fun getFreeGames(forceRefresh: Boolean = false): Flow<List<GameDto>> = flow {
        // If we aren't forcing a refresh, try to emit the cached data first.
        if (!forceRefresh) {
            val cached = database.gameQueries.selectAll().executeAsList().map { it.toDto() }
            if (cached.isNotEmpty()) {
                _dataSource.value = DataSource.CACHE
                emit(cached)
            }
        }

        try {
            // Proceed to fetch from the network.
            api.getFreeGamesFlow().collect { remoteGames ->
                val validGameIds = remoteGames.mapNotNull { it.id?.toLong() }
                notificationRepository.deleteExpiredNotifications(validGameIds)

                // Replace the entire cache with the fresh data.
                database.transaction {
                    database.gameQueries.deleteAll()
                    remoteGames.forEachIndexed { index, game ->
                        database.gameQueries.insertGame(
                            id = game.id?.toLong() ?: return@forEachIndexed,
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
                            gamerpower_url = game.gamerpower_url,
                            api_order = index.toLong()
                        )
                    }
                }

                _dataSource.value = DataSource.NETWORK
                // Emit the newly updated data from the source of truth (the database).
                val newCache = database.gameQueries.selectAll().executeAsList().map { it.toDto() }
                emit(newCache)
            }
        } catch (e: Exception) {
            // If the network call fails, the flow will either have already emitted a cached value
            // or it will complete without an emission, which will cause .first() to fail,
            // which is caught in the ViewModel. This is acceptable behavior.
            println("API failed, using cached data if available: ${e.message}")
            // We set the data source to cache to reflect the state if the API call fails.
            _dataSource.value = DataSource.CACHE
            // If we get here on a force-refresh, we should re-throw so the ViewModel knows it failed.
            if(forceRefresh) throw e
        }
    }

}
