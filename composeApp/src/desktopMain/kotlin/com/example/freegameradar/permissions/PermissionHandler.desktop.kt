package com.example.freegameradar.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

class DesktopPermissionHandler : PermissionHandler {
    override fun requestNotificationPermission(onResult: (Boolean) -> Unit) {
        // Desktop notifications don't require runtime permission, so we can immediately
        // invoke the callback with `true` to indicate permission is "granted."
        println("Desktop: notification permission automatically granted.")
        onResult(true)
    }

    override fun isNotificationPermissionGranted(): Boolean {
        // On desktop, we can consider notification permissions to always be granted.
        return true
    }
}

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    return remember { DesktopPermissionHandler() }
}
