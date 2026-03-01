package com.radarlabs.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.data.models.WorthDto
import com.radarlabs.freegameradar.data.remote.ApiService
import com.radarlabs.freegameradar.data.repository.GameRepository
import com.radarlabs.freegameradar.data.state.DataSource
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
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

    val totalWorth: StateFlow<WorthDto?> = repository.totalWorth

    private val _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    val allGames: StateFlow<List<GameDto>> = _allGames.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var refreshCount = 0

    private val _showRefreshAd = Channel<Unit>(Channel.BUFFERED)
    val showRefreshAd = _showRefreshAd.receiveAsFlow()

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
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _lastRefreshTime = MutableStateFlow(0L)
    val lastRefreshTime: StateFlow<Long> = _lastRefreshTime.asStateFlow()

    private val _canRefresh = MutableStateFlow(true)
    val canRefresh: StateFlow<Boolean> = _canRefresh.asStateFlow()

    private val _remainingCooldown = MutableStateFlow(0)
    val remainingCooldown: StateFlow<Int> = _remainingCooldown.asStateFlow()

    private val REFRESH_COOLDOWN_MS = 10_000L

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                val lastRefresh = _lastRefreshTime.value

                if (lastRefresh > 0L) {
                    val elapsed = System.currentTimeMillis() - lastRefresh
                    val cooldownExpired = elapsed >= REFRESH_COOLDOWN_MS

                    _canRefresh.value = cooldownExpired

                    if (!cooldownExpired) {
                        val remaining = ((REFRESH_COOLDOWN_MS - elapsed) / 1000).toInt()
                        _remainingCooldown.value = max(0, remaining)
                    } else {
                        _remainingCooldown.value = 0
                    }
                } else {
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
                    if (gameList.isNotEmpty()) {
                        _allGames.value = gameList
                        println("✅ Loaded ${gameList.size} games")
                    } else {
                        println("⚠️ Skipped empty game list emission")
                    }
                }
        }
    }

    fun refreshGames() {
        if (!_canRefresh.value || _isRefreshing.value) {
            println("⚠️ Refresh blocked: canRefresh=${_canRefresh.value}, isRefreshing=${_isRefreshing.value}")
            return
        }

        viewModelScope.launch {
            _isRefreshing.value = true
            println("🔄 Starting refresh...")

            try {
                repository.getFreeGames(forceRefresh = true)
                    .catch { e ->
                        println("❌ Refresh API error: ${e.message}")
                        e.printStackTrace()
                        throw e
                    }
                    .collect { freshGames ->
                        if (freshGames.isNotEmpty()) {
                            _allGames.value = freshGames
                            println("✅ Refresh successful: ${freshGames.size} games loaded")
                        } else {
                            println("⚠️ Refresh returned empty list, keeping existing games")
                        }
                    }

                _lastRefreshTime.value = System.currentTimeMillis()

                refreshCount++
                if (refreshCount % 10 == 0) {
                    _showRefreshAd.send(Unit)
                }

                println("⏱️ Cooldown started, next refresh available in ${REFRESH_COOLDOWN_MS / 1000}s")

            } catch (e: Exception) {
                println("❌ Refresh failed: ${e.message}")
                e.printStackTrace()

            } finally {
                _isRefreshing.value = false
                println("🏁 Refresh completed, isRefreshing=false")
            }
        }
    }

    @Deprecated("Use remainingCooldown StateFlow", ReplaceWith("remainingCooldown.value"))
    fun getRemainingCooldown(): Int = _remainingCooldown.value

    fun updateFilter(filter: GameTypeFilter) {
        _gameTypeFilter.value = filter
    }

    fun togglePlatform(platform: String) {
        val current = _filters.value.platforms.toMutableSet()
        if (current.contains(platform)) current.remove(platform) else current.add(platform)
        _filters.value = _filters.value.copy(platforms = current)
    }

    fun toggleType(type: String) {
        val current = _filters.value.types.toMutableSet()
        if (current.contains(type)) current.remove(type) else current.add(type)
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
