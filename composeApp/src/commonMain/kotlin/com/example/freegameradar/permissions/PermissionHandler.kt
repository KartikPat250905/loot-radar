package com.example.freegameradar.permissions

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPermissionHandler(): PermissionHandler

interface PermissionHandler {
    fun requestNotificationPermission(onResult: (Boolean) -> Unit)
    fun isNotificationPermissionGranted(): Boolean
}
