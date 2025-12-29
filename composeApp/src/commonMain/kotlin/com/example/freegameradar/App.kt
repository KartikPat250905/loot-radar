package com.example.freegameradar

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.ui.auth.AuthGate
import com.example.freegameradar.ui.components.BottomNavBar
import com.example.freegameradar.ui.components.TopBar
import com.example.freegameradar.ui.navigation.AppNavigation
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authRepository = AuthRepositoryImpl()

    AppContainer {
        AuthGate(authViewModel = authViewModel) {
            Scaffold(
                topBar = {
                    if (currentRoute != Screen.Setup.route) {
                        TopBar(navController)
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
                    authRepository = authRepository
                )
            }
        }
    }
}
