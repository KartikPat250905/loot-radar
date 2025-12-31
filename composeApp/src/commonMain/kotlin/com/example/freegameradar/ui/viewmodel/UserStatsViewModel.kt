package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.repository.UserStatsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class UserStatsViewModel(private val userStatsRepository: UserStatsRepository) : ViewModel() {

    val claimedValue: StateFlow<Float> = userStatsRepository.getClaimedValue()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0f
        )

    fun syncClaimedValue() {
        viewModelScope.launch {
            userStatsRepository.syncClaimedValue()
        }
    }

    fun addToClaimedValue(worth: Float) {
        viewModelScope.launch {
            try {
                userStatsRepository.addToClaimedValue(worth)
            } catch (e: Exception) {
                // Optionally, handle the error in the UI, e.g., show a toast.
                println("Error adding to claimed value: ${e.message}")
            }
        }
    }
}
