package com.radarlabs.freegameradar.review

class ReviewPromptPolicy(
    private val repository: ReviewSettingsRepository
) {
    companion object {
        const val MIN_CLAIMS_REQUIRED = 2
        const val MIN_DAYS_AFTER_INSTALL = 3L
        const val MIN_DAYS_BETWEEN_PROMPTS = 60L
        const val MAX_PROMPTS_TOTAL = 3
        private const val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L
    }

    fun shouldPrompt(currentTimeMillis: Long): Boolean {
        val firstInstall = repository.firstInstallTimestamp
        if (firstInstall == 0L) return false

        val claimCount = repository.successfulClaimCount
        if (claimCount < MIN_CLAIMS_REQUIRED) return false

        val daysSinceInstall = (currentTimeMillis - firstInstall) / MILLIS_IN_DAY
        if (daysSinceInstall < MIN_DAYS_AFTER_INSTALL) return false

        val promptCount = repository.reviewPromptCount
        if (promptCount >= MAX_PROMPTS_TOTAL) return false

        val lastPrompt = repository.lastReviewPromptTimestamp
        if (lastPrompt != 0L) {
            val daysSinceLastPrompt = (currentTimeMillis - lastPrompt) / MILLIS_IN_DAY
            if (daysSinceLastPrompt < MIN_DAYS_BETWEEN_PROMPTS) return false
        }

        return true
    }

    fun recordSuccessfulClaim() {
        repository.successfulClaimCount += 1
    }

    fun recordPromptShown(currentTimeMillis: Long) {
        repository.lastReviewPromptTimestamp = currentTimeMillis
        repository.reviewPromptCount += 1
    }

    fun resetCounters() {
        repository.resetAll()
    }
}
