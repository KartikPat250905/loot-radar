package com.example.lootradarkmp.ui.navigation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.lootradarkmp.ui.screens.Browse
import com.example.lootradarkmp.ui.screens.GameDetailScreen
import com.example.lootradarkmp.ui.screens.HomeScreen
import com.example.lootradarkmp.ui.screens.Notification
import com.example.lootradarkmp.ui.screens.Settings

@Composable
fun AppNavigation(navController: NavHostController, innerPadding: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
        composable(Screen.Details.route) {
            GameDetailScreen(
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
        composable(Screen.Browse.route) {
            Browse(
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
    }
}
