package com.example.freegameradar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.data.repository.UserSettingsRepositoryImpl
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AdFreeViewModel
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private lateinit var adManager: AdManager
    private lateinit var adFreeViewModel: AdFreeViewModel

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // All repositories are now created here, in the Android entry point
        val authRepository = AuthRepositoryImpl()
        val userSettingsRepository = UserSettingsRepositoryImpl(authRepository, this)
        val authViewModel = AuthViewModel(authRepository)

        adFreeViewModel = AdFreeViewModel(authRepository)

        adManager = AdManager(this) {
            !adFreeViewModel.adFreeStatus.value.isAdFree
        }

        adManager.loadGameDetailAd()
        adManager.loadRefreshAd()
        adManager.loadSettingsAd()

        val startRoute = intent.getStringExtra("route")

        setContent {
            ModernDarkTheme {
                val currentUser by authViewModel.currentUser.collectAsState()

                LaunchedEffect(currentUser) {
                    currentUser?.let {
                        userSettingsRepository.syncUserSettings()
                        adFreeViewModel.loadAdFreeStatus()
                        if (!it.isAnonymous) {
                            val userSettings = userSettingsRepository.getSettings().first()
                            if (userSettings.notificationsEnabled &&
                                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                ContextCompat.checkSelfPermission(
                                    this@MainActivity,
                                    Manifest.permission.POST_NOTIFICATIONS
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                askNotificationPermission()
                            }
                        }
                    }
                }

                // Pass repository instances to the common App composable
                App(
                    authViewModel = authViewModel,
                    adFreeViewModel = adFreeViewModel,
                    authRepository = authRepository,
                    userSettingsRepository = userSettingsRepository,
                    startRoute = startRoute,
                    onShowRefreshAd = ::showRefreshAd,
                    onShowSettingsAd = ::showSettingsAd,
                    onShowGameDetailAd = ::showGameDetailAd
                )
            }
        }
    }

    private fun showGameDetailAd() {
        Log.d("MainActivity", "🎬 showGameDetailAd() called")
        adManager.showGameDetailAd(this)
    }

    private fun showRefreshAd() {
        Log.d("MainActivity", "🎬 showRefreshAd() called")
        adManager.showRefreshAd(this)
    }

    private fun showSettingsAd() {
        Log.d("MainActivity", "🎬 showSettingsAd() called from SettingsScreen callback")
        adManager.showSettingsAd(this)
    }
}
