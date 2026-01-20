package com.example.freegameradar.util

import androidx.compose.runtime.Composable

@Composable
expect fun isNotificationPermissionGranted(): Boolean

/**
 * A Composable function that returns a launcher function.
 * This launcher, when called, will trigger the system's permission request dialog.
 * The onResult lambda is invoked with the result of the user's choice.
 */
@Composable
expect fun rememberPermissionRequestLauncher(onResult: (isGranted: Boolean) -> Unit): () -> Unit
