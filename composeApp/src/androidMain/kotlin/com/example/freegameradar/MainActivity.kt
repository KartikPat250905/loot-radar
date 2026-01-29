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
import androidx.lifecycle.viewmodel.compose.viewModel
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

        // ✅ Initialize AdFreeViewModel
        adFreeViewModel = AdFreeViewModel(authRepository)

        // ✅ Initialize AdManager with ad-free check
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
                val adFreeStatus by adFreeViewModel.adFreeStatus.collectAsState()

                // ✅ Reload ad-free status when user changes
                LaunchedEffect(currentUser) {
                    currentUser?.let {
                        adFreeViewModel.loadAdFreeStatus()

                        if (!it.isAnonymous) {
                            val userSettings = userSettingsRepository.getSettings().first()
                            if (userSettings.notificationsEnabled) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    if (ContextCompat.checkSelfPermission(
                                            this@MainActivity,
                                            Manifest.permission.POST_NOTIFICATIONS
                                        ) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        askNotificationPermission()
                                    }
                                }
                            }
                        }
                    }
                }

                App(
                    authViewModel = authViewModel,
                    adFreeViewModel = adFreeViewModel,  // ✅ Pass to App
                    startRoute = startRoute,
                    onShowRefreshAd = ::showRefreshAd,
                    onShowSettingsAd = ::showSettingsAd,
                    onShowGameDetailAd = ::showGameDetailAd
                )
            }
        }
    }

    // Public methods to show ads
    fun showGameDetailAd() {
        Log.d("MainActivity", "🎬 showGameDetailAd() called")
        adManager.showGameDetailAd(this)
    }

    fun showRefreshAd() {
        Log.d("MainActivity", "🎬 showRefreshAd() called")
        adManager.showRefreshAd(this)
    }

    fun showSettingsAd() {
        Log.d("MainActivity", "🎬 showSettingsAd() called from SettingsScreen callback")
        adManager.showSettingsAd(this)
    }
}
