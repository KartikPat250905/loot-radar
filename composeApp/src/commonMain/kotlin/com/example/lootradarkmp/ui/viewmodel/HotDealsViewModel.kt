package com.example.lootradarkmp.ui.viewmodel

import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.repository.GameRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

class HotDealsViewModel(
    private val repository: GameRepository = GameRepository()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _allGames = MutableStateFlow<List<GameDto>>(emptyList())
    val allGames: StateFlow<List<GameDto>> = _allGames
    init {
        load()
    }

    fun load() {
        scope.launch {
            repository.getFreeGames().collect { list ->
                _allGames.value = list
            }
        }
    }

    // helpers
    fun parsePriceDouble(price : String?) : Double {
        val s = price?.replace("$","")?.replace(".", "")?.trim()
        if (s.isNullOrBlank() || s.equals("N/A")) {
            return 0.0
        }
        return s.toDoubleOrNull() ?: 0.0
    }

    private fun isExpiringSoon(endDate: String?): Boolean {
        if (endDate.isNullOrBlank() || endDate == "N/A") return false
        return try {
            val isoDateStr = endDate.replace(" ", "T").let {
                if (it.endsWith("Z")) it else "${it}Z"
            }
            val endInstant = Instant.parse(isoDateStr)
            val now = Clock.System.now()
            
            // Check if expiring in the future AND within the next 2 days
            val twoDaysFromNow = now.plus(2.days)
            endInstant > now && endInstant <= twoDaysFromNow
        } catch (e: Exception) {
            false
        }
    }

    //category flows
    val highestValue: StateFlow<List<GameDto>> = _allGames
        .map { list -> list.sortedByDescending { parsePriceDouble(it.worth) } }
        .stateIn(scope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = emptyList())

    val featured: StateFlow<List<GameDto>> = _allGames
        .map { list -> list.filter { parsePriceDouble(it.worth) >= 20.0}}
        .stateIn(scope,  started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = emptyList())

    val expiringSoon: StateFlow<List<GameDto>> = _allGames
        .map { list -> 
            list.filter { isExpiringSoon(it.end_date) }
                .sortedBy { it.end_date } 
        }
        .stateIn(scope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = emptyList())

    val trending: StateFlow<List<GameDto>> = _allGames
        .map { list -> list.sortedByDescending { it.users ?: 0 } }
        .stateIn(scope, started = kotlinx.coroutines.flow.SharingStarted.Eagerly, initialValue = emptyList())

    fun clear() {
        scope.coroutineContext.cancel()
    }
}
