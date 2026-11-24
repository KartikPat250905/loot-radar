package com.radarlabs.freegameradar.review

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeReviewSettingsRepository : ReviewSettingsRepository {
    override var successfulClaimCount: Int = 0
    override var firstInstallTimestamp: Long = 0L
    override var lastReviewPromptTimestamp: Long = 0L
    override var reviewPromptCount: Int = 0

    override fun resetAll() {
        successfulClaimCount = 0
        firstInstallTimestamp = 0L
        lastReviewPromptTimestamp = 0L
        reviewPromptCount = 0
    }
}

class ReviewPromptPolicyTest {

    private val MILLIS_IN_DAY = 24 * 60 * 60 * 1000L

    @Test
    fun testFirstClaim_returnsFalse() {
        val fakeRepo = FakeReviewSettingsRepository()
        val policy = ReviewPromptPolicy(fakeRepo)
        val now = 1000000000000L

        // Installed 3 days ago, first claim
        fakeRepo.firstInstallTimestamp = now - (3 * MILLIS_IN_DAY)
        policy.recordSuccessfulClaim()

        assertFalse(policy.shouldPrompt(now), "First claim should not trigger review prompt")
    }

    @Test
    fun testSecondClaimAfter3Days_returnsTrue() {
        val fakeRepo = FakeReviewSettingsRepository()
        val policy = ReviewPromptPolicy(fakeRepo)
        val now = 1000000000000L

        // Installed 4 days ago, 2 successful claims
        fakeRepo.firstInstallTimestamp = now - (4 * MILLIS_IN_DAY)
        policy.recordSuccessfulClaim()
        policy.recordSuccessfulClaim()

        assertTrue(policy.shouldPrompt(now), "Second claim after 3 days of install should trigger review prompt")
    }

    @Test
    fun testSecondClaimAfter1Day_returnsFalse() {
        val fakeRepo = FakeReviewSettingsRepository()
        val policy = ReviewPromptPolicy(fakeRepo)
        val now = 1000000000000L

        // Installed 1 day ago, 2 successful claims
        fakeRepo.firstInstallTimestamp = now - (1 * MILLIS_IN_DAY)
        policy.recordSuccessfulClaim()
        policy.recordSuccessfulClaim()

        assertFalse(policy.shouldPrompt(now), "Second claim after 1 day of install should not trigger review prompt")
    }

    @Test
    fun testLastAsked10DaysAgo_returnsFalse() {
        val fakeRepo = FakeReviewSettingsRepository()
        val policy = ReviewPromptPolicy(fakeRepo)
        val now = 1000000000000L

        fakeRepo.firstInstallTimestamp = now - (30 * MILLIS_IN_DAY)
        fakeRepo.successfulClaimCount = 3
        fakeRepo.lastReviewPromptTimestamp = now - (10 * MILLIS_IN_DAY)
        fakeRepo.reviewPromptCount = 1

        assertFalse(policy.shouldPrompt(now), "Prompted 10 days ago should not trigger review prompt")
    }

    @Test
    fun testLastAsked61DaysAgo_returnsTrue() {
        val fakeRepo = FakeReviewSettingsRepository()
        val policy = ReviewPromptPolicy(fakeRepo)
        val now = 1000000000000L

        fakeRepo.firstInstallTimestamp = now - (100 * MILLIS_IN_DAY)
        fakeRepo.successfulClaimCount = 3
        fakeRepo.lastReviewPromptTimestamp = now - (61 * MILLIS_IN_DAY)
        fakeRepo.reviewPromptCount = 1

        assertTrue(policy.shouldPrompt(now), "Prompted 61 days ago should trigger review prompt")
    }

    @Test
    fun testAsked3TimesAlready_returnsFalse() {
        val fakeRepo = FakeReviewSettingsRepository()
        val policy = ReviewPromptPolicy(fakeRepo)
        val now = 1000000000000L

        fakeRepo.firstInstallTimestamp = now - (300 * MILLIS_IN_DAY)
        fakeRepo.successfulClaimCount = 5
        fakeRepo.lastReviewPromptTimestamp = now - (65 * MILLIS_IN_DAY)
        fakeRepo.reviewPromptCount = 3

        assertFalse(policy.shouldPrompt(now), "Already asked 3 times should not trigger review prompt")
    }
}
