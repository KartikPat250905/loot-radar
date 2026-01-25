package com.example.freegameradar.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.screens.AboutScreen
import com.example.freegameradar.ui.screens.GameDetailScreen
import com.example.freegameradar.ui.screens.HomeScreen
import com.example.freegameradar.ui.screens.HotDealsScreen
import com.example.freegameradar.ui.screens.NotificationPermissionBenefitsScreen
import com.example.freegameradar.ui.screens.NotificationScreen
import com.example.freegameradar.ui.screens.RedeemCodeScreen
import com.example.freegameradar.ui.screens.SettingsScreen
import com.example.freegameradar.ui.screens.SetupScreen
import com.example.freegameradar.ui.screens.StatsScreen
import com.example.freegameradar.ui.screens.SupportScreen
import com.example.freegameradar.ui.viewmodel.GameViewModel
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
    gameViewModel: GameViewModel, // Added
    userPreferencesViewModel: UserPreferencesViewModel,
    notificationViewModel: NotificationViewModel,
    userStatsViewModel: UserStatsViewModel,
    settingsViewModel: SettingsViewModel,
    setupViewModel: SetupViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    startRoute: String? = null
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Gate.route // The start destination is now fixed.
    ) {
        composable(Screen.Gate.route) {
            val preferencesState by userPreferencesViewModel.uiState.collectAsState()

            AppLoadingScreen() // Show a loading screen while we decide where to go.

            LaunchedEffect(preferencesState.isLoaded) {
                if (preferencesState.isLoaded) {
                    val destination = if (startRoute == "notification") {
                        Screen.Notification.route
                    } else {
                        if (preferencesState.setupComplete) Screen.Home.route else Screen.Setup.route
                    }
                    navController.navigate(destination) {
                        popUpTo(Screen.Gate.route) { inclusive = true }
                    }
                }
            }
        }

        composable(Screen.Setup.route) {
            SetupScreen(
                viewModel = setupViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.PostSetupCheck.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PostSetupCheck.route) {
            val permissionGranted = isNotificationPermissionGranted()
            AppLoadingScreen()
            LaunchedEffect(Unit) {
                val destination = if (permissionGranted) Screen.Home.route else Screen.NotificationBenefits.route
                navController.navigate(destination) { popUpTo(Screen.PostSetupCheck.route) { inclusive = true } }
            }
        }

        composable(Screen.NotificationBenefits.route) {
            var showSettingsDialog by remember { mutableStateOf(false) }
            val requestPermissionLauncher = rememberPermissionRequestLauncher { result ->
                when (result) {
                    PermissionRequestResult.GRANTED -> navController.navigate(Screen.Home.route) { popUpTo(Screen.NotificationBenefits.route) { inclusive = true } }
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
                onSkip = { navController.navigate(Screen.Home.route) { popUpTo(Screen.NotificationBenefits.route) { inclusive = true } } }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                gameViewModel = gameViewModel, // Passed in
                modifier = Modifier.padding(innerPadding),
                onBottomBarVisibilityChange = onBottomBarVisibilityChange
            )
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

        composable("support") {
            SupportScreen(
                onNavigateToRedeem = { navController.navigate("redeem") },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding) // ADD THIS
            )
        }

        composable("redeem") {
            RedeemCodeScreen(
                onSuccess = {
                    navController.popBackStack("settings", inclusive = false)
                },
                onBack = { navController.popBackStack() },
                modifier = Modifier.padding(innerPadding) // ADD THIS
            )
        }
    }
}
