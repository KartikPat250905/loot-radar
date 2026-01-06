package com.example.freegameradar.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.freegameradar.ui.screens.GameDetailScreen
import com.example.freegameradar.ui.screens.HomeScreen
import com.example.freegameradar.ui.screens.NotificationScreen
import com.example.freegameradar.ui.screens.SettingsScreen
import com.example.freegameradar.ui.screens.SetupScreen
import com.example.freegameradar.ui.screens.StatsScreen
import com.example.freegameradar.ui.viewmodel.NotificationViewModel
import com.example.freegameradar.ui.viewmodel.SettingsViewModel
import com.example.freegameradar.ui.viewmodel.SetupViewModel
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel

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
            SetupScreen(
                viewModel = setupViewModel,
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Setup.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onBottomBarVisibilityChange = onBottomBarVisibilityChange
            )
        }

        composable(Screen.Notification.route) {
            NotificationScreen(
                viewModel = notificationViewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                viewModel = settingsViewModel,
                userPreferencesViewModel = userPreferencesViewModel,
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                viewModel = userStatsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }

        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            GameDetailScreen(
                navController = navController,
                gameId = gameId,
                userStatsViewModel = userStatsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
