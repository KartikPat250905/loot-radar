package com.example.lootradarkmp.ui.viewmodel

import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.remote.ApiService
import com.example.lootradarkmp.data.repository.GameRepository
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
    private var _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val games: StateFlow<List<GameDto>> =
        combine(_allGames, _searchQuery, _filters) { games, query, filters ->
            games.filter { game ->

                val matchesSearch =
                    query.isBlank() || game.title?.contains(query, true) == true

                val matchesPlatform =
                    filters.platforms.isEmpty() ||
                            filters.platforms.any {
                                game.platforms?.contains(it, ignoreCase = true) == true
                            }

                // FIX: 'type' in GameDto might be a single string (e.g., "Game"),
                // while your filter list has lowercase "game".
                // We need to check if the game.type equals ANY of the selected types (ignoring case).
                val matchesType =
                    filters.types.isEmpty() ||
                            filters.types.any { filterType ->
                                game.type?.equals(filterType, ignoreCase = true) == true
                            }

                matchesSearch && matchesPlatform && matchesType
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
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun clear() {
        viewModelScope.cancel()
    }
}
