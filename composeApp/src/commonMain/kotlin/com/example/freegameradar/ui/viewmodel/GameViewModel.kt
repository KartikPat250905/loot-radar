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
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    // Refresh state management
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow(0L)
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()

    private val _canRefresh = MutableStateFlow(true)
    val canRefresh: StateFlow<Boolean> = _canRefresh.asStateFlow()

    // ✅ NEW: Reactive remaining cooldown
    private val _remainingCooldown = MutableStateFlow(0)
    val remainingCooldown: StateFlow<Int> = _remainingCooldown.asStateFlow()

    // Cooldown period: 30 seconds
    private val REFRESH_COOLDOWN_MS = 30_000L

    init {
        // ✅ FIXED: Non-blocking cooldown timer with reactive updates
        viewModelScope.launch {
            while (true) {
                delay(1000) // Check every second
                val lastRefresh = _lastRefreshTime.value

                if (lastRefresh > 0L) {
                    val elapsed = System.currentTimeMillis() - lastRefresh
                    val cooldownExpired = elapsed >= REFRESH_COOLDOWN_MS

                    _canRefresh.value = cooldownExpired

                    // Update remaining seconds for UI
                    if (!cooldownExpired) {
                        val remaining = ((REFRESH_COOLDOWN_MS - elapsed) / 1000).toInt()
                        _remainingCooldown.value = max(0, remaining)
                    } else {
                        _remainingCooldown.value = 0
                    }
                } else {
                    // No refresh has occurred yet
                    _canRefresh.value = true
                    _remainingCooldown.value = 0
                }
            }
        }
    }

    fun loadGames() {
        viewModelScope.launch {
            repository.getFreeGames()
                .catch { e ->
                    println("❌ Error fetching games: ${e.message}")
                    e.printStackTrace()
                }
                .collect { gameList ->
                    _allGames.value = gameList
                    println("✅ Loaded ${gameList.size} games")
                }
        }
    }

    fun refreshGames() {
        // Prevent refresh if still in cooldown or already refreshing
        if (!_canRefresh.value || _isRefreshing.value) {
            println("⚠️ Refresh blocked: canRefresh=${_canRefresh.value}, isRefreshing=${_isRefreshing.value}")
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true

            println("🔄 Starting refresh...")

            try {
                // Force refresh from API
                repository.getFreeGames(forceRefresh = true)
                    .catch { e ->
                        println("❌ Refresh API error: ${e.message}")
                        e.printStackTrace()
                        throw e // Re-throw to outer catch
                    }
                    .collect { freshGames ->
                        _allGames.value = freshGames
                        println("✅ Refresh successful: ${freshGames.size} games loaded")
                    }

                // Update last refresh time (starts cooldown)
                _lastRefreshTime.value = System.currentTimeMillis()

                println("⏱️ Cooldown started, next refresh available in ${REFRESH_COOLDOWN_MS / 1000}s")

            } catch (e: Exception) {
                println("❌ Refresh failed: ${e.message}")
                e.printStackTrace()
                // Don't re-enable canRefresh - let cooldown expire naturally

            } finally {
                _isRefreshing.value = false
                println("🏁 Refresh completed, isRefreshing=false")
            }
        }
    }

    // ✅ DEPRECATED: Use remainingCooldown StateFlow instead
    @Deprecated("Use remainingCooldown StateFlow", ReplaceWith("remainingCooldown.value"))
    fun getRemainingCooldown(): Int {
        return _remainingCooldown.value
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
