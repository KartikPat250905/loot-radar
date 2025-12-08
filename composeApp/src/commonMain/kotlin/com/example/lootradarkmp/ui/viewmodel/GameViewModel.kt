package com.example.lootradarkmp.ui.viewmodel

import com.example.lootradarkmp.data.models.GameDto
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

class GameViewModel(
    private val repository: GameRepository = GameRepository()
) {
    private val viewModelScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )
    private var _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

    val games: StateFlow<List<GameDto>> =
        combine(_allGames, _searchQuery) { games, query ->
            if (query.isBlank()){
                games
            }
            else {
                games.filter { it.title?.contains(query, true) == true}
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

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun clear() {
        viewModelScope.cancel()
    }
}
