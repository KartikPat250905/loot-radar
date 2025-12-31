package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.model.PlatformStat
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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

    val platformStats: StateFlow<List<PlatformStat>> = combine(
        claimedGameIds,
        gameRepository.getFreeGames()
    ) { claimedIds, allGames ->
        val claimedGames = allGames.filter { it.id?.toLong() in claimedIds }
        claimedGames.groupBy { it.platforms }
            .map { (platform, games) ->
                PlatformStat(
                    platform = platform ?: "Unknown",
                    count = games.size,
                    totalWorth = games.sumOf { it.worth?.replace("$", "")?.toDoubleOrNull() ?: 0.0 }
                )
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )


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
                // Optionally, handle the error in the UI, e.g., show a toast.
                println("Error adding to claimed value: ${e.message}")
            }
        }
    }
}
