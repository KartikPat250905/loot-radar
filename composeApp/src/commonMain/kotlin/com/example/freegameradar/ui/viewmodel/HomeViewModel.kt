package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.repository.GameRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val gameRepository: GameRepository
) : KmpViewModel() { // Inherit from KmpViewModel

    private val _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Filtered games based on search query
    val games: StateFlow<List<GameDto>> = combine(
        _allGames,
        _searchQuery
    ) { games, query ->
        if (query.isBlank()) {
            games
        } else {
            games.filter { game ->
                game.title?.contains(query, ignoreCase = true) == true ||
                game.description?.contains(query, ignoreCase = true) == true ||
                game.platforms?.contains(query, ignoreCase = true) == true
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadGames()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun loadGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                gameRepository.getFreeGames().collect { gameList ->
                    _allGames.value = gameList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load games"
                _isLoading.value = false
            }
        }
    }

    fun refresh() {
        loadGames()
    }
}
