package com.example.freegameradar.data.repository

import com.example.freegameradar.data.auth.AuthRepository
import com.russhwolf.settings.ObservableSettings
import kotlinx.coroutines.flow.Flow

expect class UserStatsRepository(authRepository: AuthRepository, settings: ObservableSettings) {

    fun getClaimedValue(): Flow<Float>

    fun getClaimedGameIds(): Flow<List<Long>>

    suspend fun syncClaimedValue()

    suspend fun addToClaimedValue(gameId: Long, worth: Float)

}
