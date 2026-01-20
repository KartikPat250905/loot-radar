package com.example.freegameradar.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.freegameradar.ui.screens.AboutScreen
import com.example.freegameradar.ui.screens.GameDetailScreen
import com.example.freegameradar.ui.screens.HomeScreen
import com.example.freegameradar.ui.screens.HotDealsScreen
import com.example.freegameradar.ui.screens.NotificationPermissionBenefitsScreen
import com.example.freegameradar.ui.screens.NotificationScreen
import com.example.freegameradar.ui.screens.SettingsScreen
import com.example.freegameradar.ui.screens.SetupScreen
import com.example.freegameradar.ui.screens.StatsScreen
import com.example.freegameradar.ui.viewmodel.NotificationViewModel
import com.example.freegameradar.ui.viewmodel.SettingsViewModel
import com.example.freegameradar.ui.viewmodel.SetupViewModel
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel
import com.example.freegameradar.util.PermissionRequestResult
import com.example.freegameradar.util.isNotificationPermissionGranted
import com.example.freegameradar.util.openAppSettings
import com.example.freegameradar.util.rememberPermissionRequestLauncher

@Composable
fun AppNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String,
    notificationViewModel: NotificationViewModel,
    userStatsViewModel: UserStatsViewModel,
    settingsViewModel: SettingsViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
    setupViewModel: SetupViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Setup.route) {
            val permissionGranted = isNotificationPermissionGranted()
            SetupScreen(
                viewModel = setupViewModel,
                onNavigateToHome = {
                    if (permissionGranted) {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.Setup.route) { inclusive = true } }
                    } else {
                        navController.navigate(Screen.NotificationBenefits.route) { popUpTo(Screen.Setup.route) { inclusive = true } }
                    }
                }
            )
        }

        composable(Screen.NotificationBenefits.route) {
            var showSettingsDialog by remember { mutableStateOf(false) }

            val requestPermissionLauncher = rememberPermissionRequestLauncher { result ->
                when (result) {
                    PermissionRequestResult.GRANTED -> {
                        navController.navigate(Screen.Home.route) { popUpTo(Screen.NotificationBenefits.route) { inclusive = true } }
                    }
                    PermissionRequestResult.PERMANENTLY_DENIED -> {
                        showSettingsDialog = true
                    }
                    PermissionRequestResult.DENIED -> {
                        // The user denied, but we can ask again. Do nothing and stay on the screen.
                    }
                }
            }

            if (showSettingsDialog) {
                AlertDialog(
                    onDismissRequest = { showSettingsDialog = false },
                    title = { Text("Permission Required") },
                    text = { Text("To get notifications, you must enable the permission in your phone's settings.") },
                    confirmButton = {
                        TextButton(onClick = { openAppSettings() }) {
                            Text("Open Settings")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showSettingsDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            NotificationPermissionBenefitsScreen(
                onEnableNotifications = { requestPermissionLauncher() },
                onSkip = {
                    navController.navigate(Screen.Home.route) { popUpTo(Screen.NotificationBenefits.route) { inclusive = true } }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController, modifier = Modifier.padding(innerPadding), onBottomBarVisibilityChange = onBottomBarVisibilityChange)
        }
        composable(Screen.Notification.route) {
            NotificationScreen(viewModel = notificationViewModel, navController = navController, modifier = Modifier.padding(innerPadding))
        }
        composable(Screen.HotDeals.route) {
            HotDealsScreen(navController = navController, modifier = Modifier.padding(innerPadding))
        }
        composable(Screen.Settings.route) {
            SettingsScreen(viewModel = settingsViewModel, userPreferencesViewModel = userPreferencesViewModel, navController = navController, modifier = Modifier.padding(innerPadding))
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.Stats.route) {
            StatsScreen(viewModel = userStatsViewModel, modifier = Modifier.padding(innerPadding))
        }
        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            GameDetailScreen(navController = navController, gameId = gameId, userStatsViewModel = userStatsViewModel, modifier = Modifier.padding(innerPadding))
        }
    }
}