package com.example.freegameradar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.freegameradar.core.LocalSettings
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.ui.auth.AuthGate
import com.example.freegameradar.ui.components.BottomNavBar
import com.example.freegameradar.ui.components.TopBar
import com.example.freegameradar.ui.navigation.AppNavigation
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import com.example.freegameradar.ui.viewmodel.NotificationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    authViewModel: AuthViewModel,
    startRoute: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authRepository = AuthRepositoryImpl()

    val startDestination = startRoute ?: if (LocalSettings.isSetupComplete) Screen.Home.route else Screen.Setup.route

    AppContainer { notificationRepository ->
        val notificationViewModel: NotificationViewModel = viewModel { NotificationViewModel(notificationRepository) }

        AuthGate(authViewModel = authViewModel) {
            Scaffold(
                topBar = {
                    if (currentRoute != Screen.Setup.route) {
                        TopBar(navController, notificationViewModel)
                    }
                },
                bottomBar = {
                    if (currentRoute != Screen.Setup.route) {
                        BottomNavBar(navController)
                    }
                }
            ) { innerPadding ->
                AppNavigation(
                    navController = navController,
                    innerPadding = innerPadding,
                    authRepository = authRepository,
                    startDestination = startDestination,
                    notificationRepository = notificationRepository
                )
            }
        }
    }
}
