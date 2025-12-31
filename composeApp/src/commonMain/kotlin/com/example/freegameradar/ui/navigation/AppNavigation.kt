package com.example.freegameradar.ui.navigation
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.freegameradar.core.LocalSettings
import com.example.freegameradar.data.auth.AuthRepository
import com.example.freegameradar.data.repository.UserSettingsRepositoryImpl
import com.example.freegameradar.ui.screens.HotDealsScreen
import com.example.freegameradar.ui.screens.GameDetailScreen
import com.example.freegameradar.ui.screens.HomeScreen
import com.example.freegameradar.ui.screens.NotificationScreen
import com.example.freegameradar.ui.screens.Settings
import com.example.freegameradar.ui.screens.SetupScreen
import com.example.freegameradar.ui.screens.StatsScreen
import com.example.freegameradar.ui.viewmodel.NotificationViewModel
import com.example.freegameradar.ui.viewmodel.SetupViewModel

@Composable
fun AppNavigation(
    navController: NavHostController, 
    innerPadding: PaddingValues, 
    authRepository: AuthRepository,
    startDestination: String,
    notificationViewModel: NotificationViewModel
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Setup.route) {
            SetupScreen(
                viewModel = SetupViewModel(
                    userSettingsRepository = UserSettingsRepositoryImpl(authRepository),
                    authRepository = authRepository
                ),
                onNavigateToHome = { 
                    LocalSettings.isSetupComplete = true
                    navController.navigate(Screen.Home.route) { 
                        popUpTo(Screen.Setup.route) { inclusive = true } 
                    }
                }
            )
        }
        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                modifier = Modifier.padding(innerPadding)

            )
        }
        composable(Screen.Notification.route) {
            NotificationScreen(
                viewModel = notificationViewModel,
                navController = navController, // Pass the NavController
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
        composable(Screen.Stats.route) {
            StatsScreen(modifier = Modifier.padding(innerPadding))
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
