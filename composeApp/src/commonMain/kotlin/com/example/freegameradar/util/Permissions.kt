package com.radarlabs.freegameradar.util

import androidx.compose.runtime.Composable

/**
 * Represents the result of a permission request to distinguish between a simple denial
 * and a permanent denial where the user has selected "Don't ask again."
 */
enum class PermissionRequestResult {
    GRANTED,
    DENIED, // User denied the permission, but can be asked again.
    PERMANENTLY_DENIED // User denied and selected "Don't ask again."
}

@Composable
expect fun isNotificationPermissionGranted(): Boolean

/**
 * A Composable function that returns a launcher function.
 * This launcher, when called, will trigger the system's permission request dialog.
 * The onResult lambda is invoked with a [PermissionRequestResult] detailing the user's choice.
 */
@Composable
expect fun rememberPermissionRequestLauncher(onResult: (result: PermissionRequestResult) -> Unit): () -> Unit

/**
 * Opens the platform-specific settings screen for this application.
 */
expect fun openAppSettings()
