package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.getFloatFlow
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.coroutines.toFlowSettings
import com.russhwolf.settings.get
import com.russhwolf.settings.set
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class UserStatsRepository actual constructor(
    private val authRepository: AuthRepository, 
    private val settings: ObservableSettings
) {

    private val flowSettings: FlowSettings = settings.toFlowSettings()

    companion object {
        private const val CLAIMED_VALUE_KEY = "claimed_value"
        private const val CLAIMED_GAMES_KEY = "claimed_games"
    }

    actual fun getClaimedValue(): Flow<Float> {
        return flowSettings.getFloatFlow(CLAIMED_VALUE_KEY, 0f)
    }

    actual fun getClaimedGameIds(): Flow<List<Long>> {
        return flowSettings.getStringFlow(CLAIMED_GAMES_KEY, "[]").map {
            Json.decodeFromString<List<Long>>(it)
        }
    }

    actual suspend fun syncClaimedValue() {
        // No-op on desktop
    }

    actual suspend fun addToClaimedValue(gameId: Long, worth: Float) {
        val currentClaimedValue = settings.get(CLAIMED_VALUE_KEY, 0f)
        val currentClaimedGames = settings.get(CLAIMED_GAMES_KEY, "[]")
        val gameIds = Json.decodeFromString<List<Long>>(currentClaimedGames)

        if (!gameIds.contains(gameId)) {
            val newGameIds = gameIds + gameId
            settings[CLAIMED_VALUE_KEY] = currentClaimedValue + worth
            settings[CLAIMED_GAMES_KEY] = Json.encodeToString(newGameIds)
        }
    }
}
