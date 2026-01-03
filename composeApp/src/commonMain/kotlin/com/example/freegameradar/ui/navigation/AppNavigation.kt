package com.example.freegameradar.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.freegameradar.ui.screens.HomeScreen
import com.example.freegameradar.ui.viewmodel.*

@Composable
fun AppNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    startDestination: String,
    homeViewModel: HomeViewModel,
    notificationViewModel: NotificationViewModel,
    userStatsViewModel: UserStatsViewModel,
    settingsViewModel: SettingsViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
    setupViewModel: SetupViewModel,
    onBottomBarVisibilityChange: (Boolean) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = Modifier.padding(innerPadding)
    ) {
        // Setup Screen
        composable(Screen.Setup.route) {
            onBottomBarVisibilityChange(false)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Setup Screen", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Home Screen
        composable(Screen.Home.route) {
            onBottomBarVisibilityChange(true)
            HomeScreen(homeViewModel)
        }

        // Hot Deals Screen
        composable(Screen.HotDeals.route) {
            onBottomBarVisibilityChange(true)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Hot Deals Screen", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Stats Screen
        composable(Screen.Stats.route) {
            onBottomBarVisibilityChange(true)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Stats Screen", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            onBottomBarVisibilityChange(true)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Settings Screen", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Notifications Screen
        composable(Screen.Notification.route) {
            onBottomBarVisibilityChange(true)
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Notifications Screen", style = MaterialTheme.typography.headlineMedium)
            }
        }

        // Add more screens as needed
        // composable(Screen.GameDetail.route + "/{gameId}") { backStackEntry ->
        //     val gameId = backStackEntry.arguments?.getString("gameId")
        //     GameDetailScreen(gameId = gameId, navController = navController)
        // }
    }
}
