package com.example.freegameradar.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class IOSPermissionHandler : PermissionHandler {
    override fun requestNotificationPermission(onResult: (isGranted: Boolean) -> Unit) {
        // TODO: Implement iOS permission request logic.
        // For now, we'll assume it's granted for development purposes.
        onResult(true)
    }
}

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    return remember { IOSPermissionHandler() }
}
