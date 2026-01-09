package com.example.freegameradar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.image.AppImageLoader
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.data.repository.UserSettingsRepositoryImpl
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val authRepository = AuthRepositoryImpl()
        val userSettingsRepository = UserSettingsRepositoryImpl(authRepository)
        val authViewModel = AuthViewModel(authRepository)

        val startRoute = intent.getStringExtra("route")

        setContent {
            // ✅ ADD: Setup Coil ImageLoader in MainActivity
            setSingletonImageLoaderFactory { context ->
                AppImageLoader.get(context)
            }

            ModernDarkTheme {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    currentUser?.let {
                        if (!it.isAnonymous) {
                            val userSettings = userSettingsRepository.getSettings().first()
                            if (userSettings.notificationsEnabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                        askNotificationPermission()
                                    }
                                }
                            }
                        }
                    }
                }

                App(
                    authViewModel = authViewModel,
                    startRoute = startRoute
                )
            }
        }
    }
}
