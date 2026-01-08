package com.example.freegameradar

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.data.repository.UserSettingsRepositoryImpl
import com.example.freegameradar.ui.auth.AuthGate
import com.example.freegameradar.ui.components.AdaptiveNavigationBar
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.components.TopBar
import com.example.freegameradar.ui.navigation.AppNavigation
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import com.example.freegameradar.ui.viewmodel.NotificationViewModel
import com.example.freegameradar.ui.viewmodel.SettingsViewModel
import com.example.freegameradar.ui.viewmodel.SetupViewModel
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel
import com.example.freegameradar.ui.viewmodel.rememberKmpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    authViewModel: AuthViewModel,
    startRoute: String? = null
) {
    ModernDarkTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        var isBottomBarVisible by remember { mutableStateOf(true) }

        val authRepository = remember { AuthRepositoryImpl() }
        val userSettingsRepository: UserSettingsRepository = remember(authRepository) { 
            UserSettingsRepositoryImpl(authRepository) 
        }

        val currentUser by authViewModel.currentUser.collectAsState()
        val userSettings by userSettingsRepository.getSettings().collectAsState(initial = null)

        // Detect if desktop
        val isDesktop = remember {
            System.getProperty("os.name")?.let { os ->
                os.contains("Windows", ignoreCase = true) ||
                os.contains("Mac", ignoreCase = true) ||
                os.contains("Linux", ignoreCase = true)
            } ?: false
        }

        if (userSettings == null) {
            AppLoadingScreen()
        } else {
            val startDestination = remember(userSettings?.setupComplete) {
                startRoute ?: if (userSettings?.setupComplete == true) Screen.Home.route else Screen.Setup.route
            }

            AppContainer { gameRepository, notificationRepository ->
                val notificationViewModel: NotificationViewModel = rememberKmpViewModel { 
                    NotificationViewModel(notificationRepository) 
                }
                val userStatsViewModel: UserStatsViewModel = rememberKmpViewModel { 
                    UserStatsViewModel(gameRepository) 
                }
                val settingsViewModel: SettingsViewModel = rememberKmpViewModel { 
                    SettingsViewModel(authRepository) 
                }
                val setupViewModel: SetupViewModel = rememberKmpViewModel { 
                    SetupViewModel(userSettingsRepository, authRepository) 
                }
                val userPreferencesViewModel: UserPreferencesViewModel = rememberKmpViewModel { 
                    UserPreferencesViewModel(userSettingsRepository) 
                }

                AuthGate(authViewModel = authViewModel) {
                    LaunchedEffect(currentUser) {
                        if (currentUser != null) {
                            userSettingsRepository.syncUserSettings()
                        }
                    }

                    if (isDesktop) {
                        // Desktop: Sidebar + Content
                        Row {
                            // Show navigation rail unless on setup screen
                            if (currentRoute != Screen.Setup.route) {
                                AdaptiveNavigationBar(navController)
                            }
                            
                            Scaffold(
                                topBar = {
                                    if (currentRoute != Screen.Setup.route) {
                                        TopBar(navController, notificationViewModel)
                                    }
                                }
                            ) { innerPadding ->
                                AppNavigation(
                                    navController = navController,
                                    innerPadding = innerPadding,
                                    startDestination = startDestination,
                                    notificationViewModel = notificationViewModel,
                                    userStatsViewModel = userStatsViewModel,
                                    settingsViewModel = settingsViewModel,
                                    userPreferencesViewModel = userPreferencesViewModel,
                                    setupViewModel = setupViewModel,
                                    onBottomBarVisibilityChange = { isBottomBarVisible = it }
                                )
                            }
                        }
                    } else {
                        // Mobile: Bottom Navigation
                        Scaffold(
                            topBar = {
                                if (currentRoute != Screen.Setup.route) {
                                    TopBar(navController, notificationViewModel)
                                }
                            },
                            bottomBar = {
                                if (currentRoute != Screen.Setup.route && isBottomBarVisible) {
                                    AdaptiveNavigationBar(navController)
                                }
                            }
                        ) { innerPadding ->
                            AppNavigation(
                                navController = navController,
                                innerPadding = innerPadding,
                                startDestination = startDestination,
                                notificationViewModel = notificationViewModel,
                                userStatsViewModel = userStatsViewModel,
                                settingsViewModel = settingsViewModel,
                                userPreferencesViewModel = userPreferencesViewModel,
                                setupViewModel = setupViewModel,
                                onBottomBarVisibilityChange = { isBottomBarVisible = it }
                            )
                        }
                    }
                }
            }
        }
    }
}
