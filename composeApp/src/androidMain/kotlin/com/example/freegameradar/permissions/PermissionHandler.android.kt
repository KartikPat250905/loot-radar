package com.example.freegameradar.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

@Composable
actual fun rememberPermissionHandler(): PermissionHandler {
    val context = LocalContext.current
    val onResultCallback = remember { mutableStateOf<(Boolean) -> Unit>({}) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            onResultCallback.value(isGranted)
        }
    )

    return remember(context, launcher) {
        object : PermissionHandler {
            override fun requestNotificationPermission(onResult: (isGranted: Boolean) -> Unit) {
                // Store the callback that the UI provides.
                onResultCallback.value = onResult

                // Launch the permission request.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    // On older OS versions, permission is granted by default.
                    onResult(true)
                }
            }

            override fun isNotificationPermissionGranted(): Boolean {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                } else {
                    true
                }
            }
        }
    }
}
