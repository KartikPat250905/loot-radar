package com.example.freegameradar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.data.repository.UserSettingsRepository
import com.example.freegameradar.data.repository.UserSettingsRepositoryImpl
import com.example.freegameradar.ui.auth.AuthGate
import com.example.freegameradar.ui.components.BottomNavBar
import com.example.freegameradar.ui.components.TopBar
import com.example.freegameradar.ui.navigation.AppNavigation
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AuthViewModel
import com.example.freegameradar.ui.viewmodel.NotificationViewModel
import com.example.freegameradar.ui.viewmodel.SettingsViewModel
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel
import com.example.freegameradar.ui.viewmodel.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    authViewModel: AuthViewModel,
    startRoute: String? = null
) {
    ModernDarkTheme { // Theme is now at the root
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Remember repositories to prevent re-initialization on recomposition
        val authRepository = remember { AuthRepositoryImpl() }
        val userSettingsRepository: UserSettingsRepository = remember(authRepository) { UserSettingsRepositoryImpl(authRepository) }

        val currentUser by authViewModel.currentUser.collectAsState()
        val userSettings by userSettingsRepository.getSettings().collectAsState(initial = null)

        if (userSettings == null) {
            Scaffold { // This will now inherit the dark theme's background
                Box(
                    modifier = Modifier.fillMaxSize().padding(it),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        } else {
            val startDestination = remember(userSettings?.setupComplete) {
                startRoute ?: if (userSettings?.setupComplete == true) Screen.Home.route else Screen.Setup.route
            }

            AppContainer { gameRepository, notificationRepository, userStatsRepository ->
                val notificationViewModel: NotificationViewModel = viewModel { NotificationViewModel(notificationRepository) }
                val userStatsViewModel: UserStatsViewModel = viewModel { UserStatsViewModel(userStatsRepository, gameRepository) }
                val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(authRepository) }
                val userPreferencesViewModel: UserPreferencesViewModel = viewModel { UserPreferencesViewModel(userSettingsRepository) }
                val setupViewModel: SetupViewModel = viewModel { SetupViewModel(userSettingsRepository, authRepository) }

                AuthGate(authViewModel = authViewModel) {
                    LaunchedEffect(currentUser) {
                        if (currentUser != null) {
                            userStatsViewModel.syncClaimedValue()
                            userSettingsRepository.syncUserSettings()
                        }
                    }

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
                            notificationViewModel = notificationViewModel,
                            userStatsViewModel = userStatsViewModel,
                            settingsViewModel = settingsViewModel,
                            userPreferencesViewModel = userPreferencesViewModel,
                            setupViewModel = setupViewModel
                        )
                    }
                }
            }
        }
    }
}
