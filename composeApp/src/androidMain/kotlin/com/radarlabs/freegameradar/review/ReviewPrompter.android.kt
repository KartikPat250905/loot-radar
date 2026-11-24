package com.radarlabs.freegameradar.review

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.withResumed
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.radarlabs.freegameradar.util.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

class AndroidReviewPrompter(
    private val activity: Activity
) : ReviewPrompter {

    override suspend fun requestReview() {
        try {
            (activity as? LifecycleOwner)?.lifecycle?.withResumed { }
            delay(800)

            val isDebug = (activity.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
            val manager: ReviewManager = if (isDebug) {
                Logger.d("ReviewPrompter", "Using FakeReviewManager for debug build")
                FakeReviewManager(activity)
            } else {
                ReviewManagerFactory.create(activity)
            }

            val reviewInfo = manager.requestReviewFlow().await()
            manager.launchReviewFlow(activity, reviewInfo).await()
            Logger.d("ReviewPrompter", "In-app review flow completed successfully")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e("ReviewPrompter", "Failed to show review prompt: ${e.message}", e)
        }
    }
}

@Composable
actual fun rememberReviewPrompter(): ReviewPrompter {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    return remember(activity) {
        if (activity != null) {
            AndroidReviewPrompter(activity)
        } else {
            object : ReviewPrompter {
                override suspend fun requestReview() {
                    Logger.d("ReviewPrompter", "No activity found to present review prompt")
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
