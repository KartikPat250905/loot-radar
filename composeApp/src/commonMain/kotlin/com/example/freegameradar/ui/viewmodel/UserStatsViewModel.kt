package com.example.freegameradar.ui.viewmodel

import com.example.freegameradar.data.model.PlatformStat
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilteredStats(
    val count: Int,
    val totalWorth: Double,
)

class UserStatsViewModel(
    private val userStatsRepository: UserStatsRepository,
    private val gameRepository: GameRepository
) : KmpViewModel() {

    private val _filter = MutableStateFlow(GameTypeFilter.ALL)
    val filter: StateFlow<GameTypeFilter> = _filter

    val claimedValue: StateFlow<Float> = userStatsRepository.getClaimedValue()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    val claimedGameIds: StateFlow<List<Long>> = userStatsRepository.getClaimedGameIds()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredStats: StateFlow<FilteredStats> = combine(gameRepository.getFreeGames(), filter) { allGames, filter ->
        val filteredGames = when (filter) {
            GameTypeFilter.ALL -> allGames
            GameTypeFilter.GAME -> allGames.filter { it.type?.lowercase() == "game" }
            GameTypeFilter.DLC -> allGames.filter { it.type?.lowercase() == "dlc" }
            GameTypeFilter.LOOT -> allGames.filter { it.type?.lowercase() == "early access" }
        }.distinctBy { it.id }

        var totalWorth = 0.0

        filteredGames.forEach { game ->
            val worth = game.worth?.replace("$", "")?.replace("N/A", "0")?.toDoubleOrNull() ?: 0.0
            totalWorth += worth
        }

        FilteredStats(
            count = filteredGames.size,
            totalWorth = totalWorth,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FilteredStats(0, 0.0)
    )

    val platformStats: StateFlow<List<PlatformStat>> = gameRepository.getFreeGames()
        .map { allGames ->
            val baseGames = allGames
                .filter { game -> game.type?.lowercase() != "dlc" }
                .distinctBy { it.id }

            val platformCounts = mutableMapOf<String, MutableList<Double>>()

            baseGames.forEach { game ->
                val worth = game.worth?.replace("$", "")?.replace("N/A", "0")?.toDoubleOrNull() ?: 0.0

                val mainPlatform = extractMainPlatform(game.platforms ?: "Unknown")
                platformCounts.getOrPut(mainPlatform) { mutableListOf() }.add(worth)
            }

            platformCounts.map { (platform, worths) ->
                PlatformStat(
                    platform = platform,
                    count = worths.size,
                    totalWorth = worths.sum()
                )
            }.sortedByDescending { it.count }

        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private fun extractMainPlatform(platformString: String): String {
        val lower = platformString.lowercase()

        // Priority 1: Specific stores/launchers
        return when {
            lower.contains("epic games store") || lower.contains("epic-games-store") -> "Epic Games"
            lower.contains("steam") -> "Steam"
            lower.contains("gog") -> "GOG"
            lower.contains("itch.io") || lower.contains("itchio") -> "Itch.io"
            lower.contains("ubisoft") -> "Ubisoft"
            lower.contains("origin") -> "EA Origin"
            lower.contains("battlenet") || lower.contains("battle.net") -> "Battle.net"

            // Priority 2: Console platforms
            lower.contains("ps5") || lower.contains("ps4") -> "PlayStation"
            lower.contains("xbox") -> "Xbox"
            lower.contains("switch") -> "Nintendo Switch"

            // Priority 4: Other platforms
            lower.contains("drm-free") || lower.contains("drm free") -> "DRM-Free"

            else -> "Other"
        }
    }

    fun updateFilter(filter: GameTypeFilter) {
        _filter.value = filter
    }

    fun syncClaimedValue() {
        viewModelScope.launch {
            userStatsRepository.syncClaimedValue()
        }
    }

    fun addToClaimedValue(gameId: Long, worth: Float) {
        viewModelScope.launch {
            try {
                userStatsRepository.addToClaimedValue(gameId, worth)
            }
            catch (e: Exception) {
                println("Error adding to claimed value: ${e.message}")
            }
        }
    }
}