package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.state.DataSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _gameTypeFilter = MutableStateFlow(GameTypeFilter.ALL)
    val gameTypeFilter: StateFlow<GameTypeFilter> = _gameTypeFilter.asStateFlow()

    // Get dataSource directly from repository
    val dataSource: StateFlow<DataSource> = gameRepository.dataSource

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Filtered games
    val games: StateFlow<List<GameDto>> = combine(
        _allGames,
        _searchQuery,
        _gameTypeFilter
    ) { games, query, filter ->
        games
            .filter { game ->
                val apiValue = filter.toApiValue()
                if (apiValue != null) {
                    game.type == apiValue
                } else {
                    true
                }
            }
            .filter { game ->
                if (query.isBlank()) {
                    true
                } else {
                    game.title?.contains(query, ignoreCase = true) == true ||
                            game.description?.contains(query, ignoreCase = true) == true ||
                            game.platforms?.contains(query, ignoreCase = true) == true
                }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadGames()
    }

    fun loadGames() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Your repository already handles cache->network flow
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

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: GameTypeFilter) {
        _gameTypeFilter.value = filter
    }

    fun refresh() {
        loadGames() // This will trigger cache->network flow in repository
    }

    fun clear() {
        // Cleanup if needed
    }
}
