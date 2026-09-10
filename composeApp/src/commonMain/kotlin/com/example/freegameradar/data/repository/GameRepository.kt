package com.radarlabs.freegameradar.data.repository

import com.radarlabs.freegameradar.core.LocalSettings
import com.radarlabs.freegameradar.data.GameDatabaseProvider
import com.radarlabs.freegameradar.data.mappers.toDto
import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.data.models.WorthDto
import com.radarlabs.freegameradar.data.remote.ApiService
import com.radarlabs.freegameradar.data.state.DataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class GameRepository(
    private val api: ApiService
) {
    private val _dataSource = MutableStateFlow(DataSource.CACHE)
    val dataSource: StateFlow<DataSource> = _dataSource

    private val _totalWorth = MutableStateFlow<WorthDto?>(
        LocalSettings.lastKnownWorth?.let { WorthDto(worthEstimationUsd = it) }
    )
    val totalWorth: StateFlow<WorthDto?> = _totalWorth

    private val database = GameDatabaseProvider.getDatabase()
    private val notificationRepository = NotificationRepository(database)

    fun getFreeGames(forceRefresh: Boolean = false): Flow<List<GameDto>> = flow {
        if (!forceRefresh) {
            val cached = database.gameQueries.selectAll().executeAsList().map { it.toDto() }
            if (cached.isNotEmpty()) {
                _dataSource.value = DataSource.CACHE
                emit(cached)
            }
        }

        try {
            coroutineScope {
                // 🚀 Fetch both games and worth in parallel for maximum speed
                val gamesDeferred = async { api.getFreeGamesFlow().first() }
                val worthDeferred = async {
                    try {
                        api.getTotalWorth()
                    } catch (_: Exception) {
                        null
                    }
                }

                val remoteGames = gamesDeferred.await()
                val worth = worthDeferred.await()

                // Update worth immediately if available
                if (worth != null) {
                    _totalWorth.value = worth
                    worth.worthEstimationUsd?.let {
                        LocalSettings.lastKnownWorth = it
                    }
                }

                // 🚀 EMIT IMMEDIATELY: Don't wait for DB operations
                _dataSource.value = DataSource.NETWORK
                emit(remoteGames)

                // Perform database operations in the background
                withContext(Dispatchers.Default) {
                    val validGameIds = remoteGames.mapNotNull { it.id?.toLong() }
                    notificationRepository.deleteExpiredNotifications(validGameIds)

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
                }
            }
        } catch (e: Exception) {
            println("API failed, using cached data if available: ${e.message}")
            _dataSource.value = DataSource.CACHE
            if (forceRefresh) throw e
        }
    }
}
