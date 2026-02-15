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
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AdFreeViewModel
import com.example.freegameradar.ui.viewmodel.AuthInitViewModel
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
                
                // ✅ Create init ViewModel
                val authInitViewModel: AuthInitViewModel = viewModel {
                    AuthInitViewModel(userSettingsRepository)
                }
                val isInitialized by authInitViewModel.isInitialized.collectAsState()

                // ✅ Trigger initialization when user changes
                LaunchedEffect(currentUser) {
                    currentUser?.let { user ->
                        // Start sync
                        authInitViewModel.initialize()
                        
                        // Other setup
                        adFreeViewModel.loadAdFreeStatus()
                        
                        if (!user.isAnonymous) {
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

                // ✅ Show loading screen until initialized
                if (currentUser != null && !isInitialized) {
                    AppLoadingScreen()
                } else {
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