package com.example.freegameradar.ui.navigation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.freegameradar.ui.screens.HotDealsScreen
import com.example.freegameradar.ui.screens.GameDetailScreen
import com.example.freegameradar.ui.screens.HomeScreen
import com.example.freegameradar.ui.screens.Notification
import com.example.freegameradar.ui.screens.Settings

@Composable
fun AppNavigation(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                modifier = Modifier.padding(innerPadding)

            )
        }
        composable(Screen.Notification.route) {
            Notification(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
        composable(Screen.HotDeals.route) {
            HotDealsScreen(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
        composable(Screen.Settings.route) {
            Settings(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
        composable(Screen.Details.route) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId")?.toLongOrNull()
            GameDetailScreen(
                navController = navController,
                gameId = gameId,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
