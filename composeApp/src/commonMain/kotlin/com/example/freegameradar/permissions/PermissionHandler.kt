package com.example.freegameradar.permissions

import androidx.compose.runtime.Composable

/**
 * A multiplatform handler for requesting runtime permissions.
 */
interface PermissionHandler {
    fun requestNotificationPermission(onResult: (isGranted: Boolean) -> Unit)
}

/**
 * Provides a platform-specific implementation of the PermissionHandler.
 */
@Composable
expect fun rememberPermissionHandler(): PermissionHandler
