package com.radarlabs.freegameradar.ui.navigation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.radarlabs.freegameradar.ui.components.AppLoadingScreen
import com.radarlabs.freegameradar.ui.screens.AboutScreen
import com.radarlabs.freegameradar.ui.screens.GameDetailScreen
import com.radarlabs.freegameradar.ui.screens.HomeScreen
import com.radarlabs.freegameradar.ui.screens.HotDealsScreen
import com.radarlabs.freegameradar.ui.screens.NotificationPermissionBenefitsScreen
import com.radarlabs.freegameradar.ui.screens.NotificationScreen
import com.radarlabs.freegameradar.ui.screens.RedeemCodeScreen
import com.radarlabs.freegameradar.ui.screens.SettingsScreen
import com.radarlabs.freegameradar.ui.screens.SetupScreen
import com.radarlabs.freegameradar.ui.screens.StatsScreen
import com.radarlabs.freegameradar.ui.screens.SupportScreen
import com.radarlabs.freegameradar.ui.viewmodel.GameViewModel
import com.radarlabs.freegameradar.ui.viewmodel.NotificationViewModel
import com.radarlabs.freegameradar.ui.viewmodel.SettingsViewModel
import com.radarlabs.freegameradar.ui.viewmodel.SetupViewModel
import com.radarlabs.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.radarlabs.freegameradar.ui.viewmodel.UserStatsViewModel
import com.radarlabs.freegameradar.util.PermissionRequestResult
import com.radarlabs.freegameradar.util.isNotificationPermissionGranted
import com.radarlabs.freegameradar.util.openAppSettings
import com.radarlabs.freegameradar.util.rememberPermissionRequestLauncher
import kotlinx.coroutines.delay

@Composable
fun AppNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    gameViewModel: GameViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
    notificationViewModel: NotificationViewModel,
    userStatsViewModel: UserStatsViewModel,
    settingsViewModel: SettingsViewModel,
    setupViewModel: SetupViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    startRoute: String? = null,
    onShowRefreshAd: () -> Unit = {},
    onShowSettingsAd: () -> Unit = {},
    onShowGameDetailAd: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Gate.route
    ) {
        composable(Screen.Gate.route) {
            Log.d("AppNavigation", "🚪 GATE ROUTE - Loading preferences")
            val preferencesState by userPreferencesViewModel.uiState.collectAsState()

            // Full screen loading with proper background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D1B2A),
                                Color(0xFF1B263B),
                                Color(0xFF0D1B2A)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppLoadingScreen(fullScreen = true)
            }

            LaunchedEffect(preferencesState.isLoaded) {
                if (preferencesState.isLoaded) {
                    // Small delay to ensure smooth transition
                    delay(150)
                    val destination = if (startRoute == "notification") {
                        Screen.Notification.route
                    } else {
                        if (preferencesState.setupComplete) Screen.Home.route else Screen.Setup.route
                    }
                    Log.d("AppNavigation", "🚪 GATE ROUTE - Navigating to: $destination (setupComplete=${preferencesState.setupComplete}, startRoute=$startRoute)")
                    navController.navigate(destination) {
                        popUpTo(Screen.Gate.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Setup.route) {
            Log.d("AppNavigation", "⚙️ SETUP ROUTE - Showing setup screen")
            SetupScreen(
                viewModel = setupViewModel,
                onNavigateToHome = {
                    Log.d("AppNavigation", "⚙️ SETUP ROUTE - Setup complete, navigating to PostSetupCheck")
                    navController.navigate(Screen.PostSetupCheck.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PostSetupCheck.route) {
            Log.d("AppNavigation", "✅ POST_SETUP_CHECK ROUTE - Checking notification permission")
            val permissionGranted = isNotificationPermissionGranted()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0D1B2A),
                                Color(0xFF1B263B),
                                Color(0xFF0D1B2A)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                AppLoadingScreen(fullScreen = true)
            }

            LaunchedEffect(Unit) {
                delay(150)
                val destination = if (permissionGranted) Screen.Home.route else Screen.NotificationBenefits.route
                Log.d("AppNavigation", "✅ POST_SETUP_CHECK ROUTE - Permission granted: $permissionGranted, navigating to: $destination")
                navController.navigate(destination) {
                    popUpTo(Screen.PostSetupCheck.route) { inclusive = true }
                }
            }
        }

        composable(Screen.NotificationBenefits.route) {
            Log.d("AppNavigation", "🔔 NOTIFICATION_BENEFITS ROUTE - Showing notification benefits screen")
            var showSettingsDialog by remember { mutableStateOf(false) }
            val requestPermissionLauncher = rememberPermissionRequestLauncher { result ->
                Log.d("AppNavigation", "🔔 NOTIFICATION_BENEFITS ROUTE - Permission result: $result")
                when (result) {
                    PermissionRequestResult.GRANTED -> navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.NotificationBenefits.route) { inclusive = true }
                    }
                    PermissionRequestResult.PERMANENTLY_DENIED -> showSettingsDialog = true
                    PermissionRequestResult.DENIED -> { /* Do nothing */ }
                }
            }

            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = { Text("Permission Required") },
                    text = { Text("To get notifications, you must enable the permission in your phone's settings.") },
                    confirmButton = { TextButton(onClick = { openAppSettings() }) { Text("Open Settings") } },
                    dismissButton = { TextButton(onClick = { showSettingsDialog = false }) { Text("Cancel") } }
                )
            }

            NotificationPermissionBenefitsScreen(
                onEnableNotifications = { requestPermissionLauncher() },
                onSkip = {
                    Log.d("AppNavigation", "🔔 NOTIFICATION_BENEFITS ROUTE - User skipped, navigating to Home")
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.NotificationBenefits.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                gameViewModel = gameViewModel,
                userPreferencesViewModel = userPreferencesViewModel,
                modifier = Modifier.padding(innerPadding),
                onBottomBarVisibilityChange = onBottomBarVisibilityChange,
                onShowRefreshAd = onShowRefreshAd
            )
        }

        composable(Screen.Notification.route) {
            NotificationScreen(viewModel = notificationViewModel, navController = navController, modifier = Modifier.padding(innerPadding))
        }

        composable(Screen.HotDeals.route) {
            HotDealsScreen(navController = navController, modifier = Modifier.padding(innerPadding))
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                userPreferencesViewModel = userPreferencesViewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onSettingsScreenOpen = onShowSettingsAd
            )
        }

        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }

        composable(Screen.Stats.route) {
            StatsScreen(viewModel = userStatsViewModel, modifier = Modifier.padding(innerPadding))
        }

        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            GameDetailScreen(
                navController = navController,
                gameId = gameId,
                userStatsViewModel = userStatsViewModel,
                modifier = Modifier.padding(innerPadding),
                onShowGameDetailAd = onShowGameDetailAd
            )
        }

        composable("support") {
            SupportScreen(
                onNavigateToRedeem = { navController.navigate("redeem") },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding)
            )
        }

        composable("redeem") {
            RedeemCodeScreen(
                onSuccess = {
                    navController.popBackStack("settings", inclusive = false)
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
