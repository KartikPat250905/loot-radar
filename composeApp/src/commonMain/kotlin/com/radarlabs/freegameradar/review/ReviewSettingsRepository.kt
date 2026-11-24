package com.radarlabs.freegameradar.review

import com.russhwolf.settings.Settings

interface ReviewSettingsRepository {
    var successfulClaimCount: Int
    var firstInstallTimestamp: Long
    var lastReviewPromptTimestamp: Long
    var reviewPromptCount: Int
    fun resetAll()
}

class ReviewSettingsRepositoryImpl(
    private val settings: Settings
) : ReviewSettingsRepository {

    companion object {
        private const val KEY_CLAIM_COUNT = "review_successful_claim_count"
        private const val KEY_FIRST_INSTALL = "review_first_install_timestamp"
        private const val KEY_LAST_PROMPT = "review_last_prompt_timestamp"
        private const val KEY_PROMPT_COUNT = "review_prompt_count"
    }

    override var successfulClaimCount: Int
        get() = settings.getInt(KEY_CLAIM_COUNT, 0)
        set(value) = settings.putInt(KEY_CLAIM_COUNT, value)

    override var firstInstallTimestamp: Long
        get() = settings.getLong(KEY_FIRST_INSTALL, 0L)
        set(value) = settings.putLong(KEY_FIRST_INSTALL, value)

    override var lastReviewPromptTimestamp: Long
        get() = settings.getLong(KEY_LAST_PROMPT, 0L)
        set(value) = settings.putLong(KEY_LAST_PROMPT, value)

    override var reviewPromptCount: Int
        get() = settings.getInt(KEY_PROMPT_COUNT, 0)
        set(value) = settings.putInt(KEY_PROMPT_COUNT, value)

    override fun resetAll() {
        settings.remove(KEY_CLAIM_COUNT)
        settings.remove(KEY_FIRST_INSTALL)
        settings.remove(KEY_LAST_PROMPT)
        settings.remove(KEY_PROMPT_COUNT)
    }
}
