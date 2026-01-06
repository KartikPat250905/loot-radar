package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.remote.ApiService
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.state.DataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameFilters(
    val platforms: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),
)

class GameViewModel(
    private val repository: GameRepository = GameRepository(ApiService())
) {
    private val _filters = MutableStateFlow(GameFilters())
    val filters: StateFlow<GameFilters> = _filters

    private val viewModelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )

    private val _gameTypeFilter = MutableStateFlow(GameTypeFilter.ALL)
    val gameTypeFilter: StateFlow<GameTypeFilter> = _gameTypeFilter

    // Add isSyncing state for loading indicator
    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    val dataSource: StateFlow<DataSource> = repository.dataSource

    private var _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val searchQuery: StateFlow<String> = _searchQuery  // Expose searchQuery

    val games: StateFlow<List<GameDto>> =
        combine(_allGames, _searchQuery, _filters, _gameTypeFilter) { games, query, filters, typeFilter ->
            games.filter { game ->

                val matchesSearch =
                    query.isBlank() || game.title?.contains(query, true) == true

                val matchesPlatform =
                    filters.platforms.isEmpty() ||
                            filters.platforms.any {
                                game.platforms?.contains(it, ignoreCase = true) == true
                            }

                val matchesType =
                    filters.types.isEmpty() ||
                            filters.types.any { filterType ->
                                game.type?.equals(filterType, ignoreCase = true) == true
                            }

                val matchesGameTypeFilter = when (typeFilter) {
                    GameTypeFilter.ALL -> true
                    GameTypeFilter.GAME -> game.type.equals("Game", ignoreCase = true)
                    GameTypeFilter.DLC -> game.type.equals("DLC", ignoreCase = true)
                    GameTypeFilter.LOOT -> game.type.equals("Early Access", ignoreCase = true)
                }

                matchesSearch && matchesPlatform && matchesType && matchesGameTypeFilter
            }
        }.stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    fun loadGames() {
        viewModelScope.launch {
            repository.getFreeGames()
                .catch { e ->
                    println("Error fetching games: ${e.message}")
                }
                .collect { gameList ->
                    _allGames.value = gameList
                }
        }
    }

    // Simplified syncFromNetwork - just reloads games with sync indicator
    fun syncFromNetwork() {
        viewModelScope.launch {
            _isSyncing.value = true
            try {
                repository.getFreeGames()
                    .catch { e ->
                        println("Error syncing games: ${e.message}")
                    }
                    .collect { gameList ->
                        _allGames.value = gameList
                    }
            } finally {
                _isSyncing.value = false
            }
        }
    }

    fun updateFilter(filter: GameTypeFilter) {
        _gameTypeFilter.value = filter
    }

    fun togglePlatform(platform: String) {
        val current = _filters.value.platforms.toMutableSet()
        if (current.contains(platform))
            current.remove(platform)
        else
            current.add(platform)
        _filters.value = _filters.value.copy(platforms = current)
    }

    fun toggleType(type: String) {
        val current = _filters.value.types.toMutableSet()
        if (current.contains(type)) {
            current.remove(type)
        } else {
            current.add(type)
        }
        _filters.value = _filters.value.copy(types = current)
    }

    fun clearFilters() {
        _filters.value = GameFilters()
        _gameTypeFilter.value = GameTypeFilter.ALL
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun clear() {
        viewModelScope.cancel()
    }
}
