package com.example.freegameradar.util

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.freegameradar.FreeGameRadarApp

@Composable
actual fun isNotificationPermissionGranted(): Boolean {
    val context = LocalContext.current
    return ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
actual fun rememberPermissionRequestLauncher(onResult: (result: PermissionRequestResult) -> Unit): () -> Unit {
    val context = LocalContext.current
    // We need an activity to check shouldShowRequestPermissionRationale
    val activity = context as? Activity

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            val result = when {
                isGranted -> PermissionRequestResult.GRANTED
                // If permission is denied and we should NOT show a rationale, it means the user has permanently denied it.
                activity?.shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) == false -> {
                    PermissionRequestResult.PERMANENTLY_DENIED
                }
                else -> {
                    // The user denied, but we can ask again.
                    PermissionRequestResult.DENIED
                }
            }
            onResult(result)
        }
    )

    return { launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS) }
}

/**
 * Opens the app-specific settings screen on the user's device.
 */
actual fun openAppSettings() {
    // This must NOT be a composable function. We need a global context from the Application class.
    val context = FreeGameRadarApp.appContext
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    context.startActivity(intent)
}
