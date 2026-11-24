package com.radarlabs.freegameradar.review

import androidx.compose.runtime.Composable

interface ReviewPrompter {
    suspend fun requestReview()
}

@Composable
expect fun rememberReviewPrompter(): ReviewPrompter
