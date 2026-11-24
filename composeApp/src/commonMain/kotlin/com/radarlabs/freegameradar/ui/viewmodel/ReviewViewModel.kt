package com.radarlabs.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.radarlabs.freegameradar.review.ReviewPromptPolicy
import com.radarlabs.freegameradar.review.ReviewSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock

data class ReviewState(
    val successfulClaimCount: Int = 0,
    val reviewPromptCount: Int = 0,
    val pendingPrompt: Boolean = false
)

class ReviewViewModel(
    private val repository: ReviewSettingsRepository
) : ViewModel() {

    val policy = ReviewPromptPolicy(repository)

    init {
        if (repository.firstInstallTimestamp == 0L) {
            repository.firstInstallTimestamp = Clock.System.now().toEpochMilliseconds()
        }
    }

    private val _uiState = MutableStateFlow(
        ReviewState(
            successfulClaimCount = repository.successfulClaimCount,
            reviewPromptCount = repository.reviewPromptCount
        )
    )
    val uiState: StateFlow<ReviewState> = _uiState.asStateFlow()

    fun onSuccessfulClaim() {
        policy.recordSuccessfulClaim()
        val now = Clock.System.now().toEpochMilliseconds()
        val shouldPrompt = policy.shouldPrompt(now)
        _uiState.value = _uiState.value.copy(
            successfulClaimCount = repository.successfulClaimCount,
            pendingPrompt = shouldPrompt
        )
    }

    fun onPromptPresented() {
        val now = Clock.System.now().toEpochMilliseconds()
        policy.recordPromptShown(now)
        _uiState.value = _uiState.value.copy(
            reviewPromptCount = repository.reviewPromptCount,
            pendingPrompt = false
        )
    }

    fun clearPendingPrompt() {
        _uiState.value = _uiState.value.copy(pendingPrompt = false)
    }

    fun resetCounters() {
        policy.resetCounters()
        val now = Clock.System.now().toEpochMilliseconds()
        val fourDaysAgo = now - (4 * 24 * 60 * 60 * 1000L)
        repository.firstInstallTimestamp = fourDaysAgo
        _uiState.value = ReviewState(
            successfulClaimCount = repository.successfulClaimCount,
            reviewPromptCount = repository.reviewPromptCount,
            pendingPrompt = false
        )
    }
}
