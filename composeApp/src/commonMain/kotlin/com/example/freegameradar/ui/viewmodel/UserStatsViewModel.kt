package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.model.PlatformStat
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserStatsViewModel(
    private val userStatsRepository: UserStatsRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

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

    // Platform stats for CURRENTLY AVAILABLE free games (not claimed)
    val platformStats: StateFlow<List<PlatformStat>> = gameRepository.getFreeGames()
        .map { allGames ->
            // Filter out DLCs and duplicates by ID to ensure each game is counted only once
            val baseGames = allGames
                .filter { game -> game.type?.lowercase() != "dlc" }
                .distinctBy { it.id }

            // Count games per platform - prioritize store platforms
            val platformCounts = mutableMapOf<String, MutableList<Double>>()

            baseGames.forEach { game ->
                val worth = game.worth?.replace("$", "")?.replace("N/A", "0")?.toDoubleOrNull() ?: 0.0

                // Extract the main platform from the platforms string
                val mainPlatform = extractMainPlatform(game.platforms ?: "Unknown")
                platformCounts.getOrPut(mainPlatform) { mutableListOf() }.add(worth)
            }

            // Convert to PlatformStat and sort by count
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

    // Extract the main platform from the platform string
    // Prioritizes store platforms (Steam, Epic, GOG, Itch.io) over device platforms (PC, Android, iOS)
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

    fun syncClaimedValue() {
        viewModelScope.launch {
            userStatsRepository.syncClaimedValue()
        }
    }

    fun addToClaimedValue(gameId: Long, worth: Float) {
        viewModelScope.launch {
            try {
                userStatsRepository.addToClaimedValue(gameId, worth)
            } catch (e: Exception) {
                println("Error adding to claimed value: ${e.message}")
            }
        }
    }
}