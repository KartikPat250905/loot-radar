package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.remote.ApiService
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.state.DataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

data class GameFilters(
    val platforms: Set<String> = emptySet(),
    val types: Set<String> = emptySet(),

    )

class GameViewModel(
    private val repository: GameRepository = GameRepository(ApiService())
) : ViewModel() {
    private val _filters = MutableStateFlow(GameFilters())
    val filters: StateFlow<GameFilters> = _filters

    private val _gameTypeFilter = MutableStateFlow(GameTypeFilter.ALL)
    val gameTypeFilter: StateFlow<GameTypeFilter> = _gameTypeFilter.asStateFlow()


    val dataSource: StateFlow<DataSource> = repository.dataSource

    private var _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    private val _searchQuery = MutableStateFlow("")

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
                    GameTypeFilter.GAMES -> game.type.equals("Game", ignoreCase = true)
                    GameTypeFilter.DLC -> game.type.equals("DLC", ignoreCase = true)
                    GameTypeFilter.EARLY_ACCESS -> game.type.equals("Early Access", ignoreCase = true)
                }

                matchesSearch && matchesPlatform && matchesType && matchesGameTypeFilter
            }
        }.stateIn(
            scope = viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    // Refresh state management
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow(0L)
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()

    private val _canRefresh = MutableStateFlow(true)
    val canRefresh: StateFlow<Boolean> = _canRefresh.asStateFlow()

    // Cooldown period: 30 seconds (adjust as needed)
    private val REFRESH_COOLDOWN_MS = 30_000L

    init {
        // Start cooldown timer
        viewModelScope.launch {
            _lastRefreshTime.collect { lastRefresh ->
                if (lastRefresh > 0) {
                    val elapsed = System.currentTimeMillis() - lastRefresh
                    _canRefresh.value = elapsed >= REFRESH_COOLDOWN_MS

                    // Update every second to show countdown
                    if (elapsed < REFRESH_COOLDOWN_MS) {
                        delay(1000)
                        _lastRefreshTime.value = lastRefresh // Trigger recheck
                    }
                }
            }
        }
    }

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

    fun refreshGames() {
        // Prevent refresh if still in cooldown
        if (!_canRefresh.value || _isRefreshing.value) {
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            _canRefresh.value = false

            try {
                // Force fetch from API (bypass cache)
                val freshGames = repository.getFreeGames(forceRefresh = true).first()
                _allGames.value = freshGames

                // Update last refresh time
                _lastRefreshTime.value = System.currentTimeMillis()

            } catch (e: Exception) {
                // Handle error (show snackbar, etc.)
                println("Refresh failed: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun getRemainingCooldown(): Int {
        val elapsed = System.currentTimeMillis() - _lastRefreshTime.value
        val remaining = (REFRESH_COOLDOWN_MS - elapsed) / 1000
        return max(0, remaining.toInt())
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
        // No-op. viewModelScope is handled by lifecycle.
    }
}