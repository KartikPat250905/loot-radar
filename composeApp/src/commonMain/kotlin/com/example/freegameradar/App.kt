package com.radarlabs.freegameradar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.radarlabs.freegameradar.data.auth.AuthRepository
import com.radarlabs.freegameradar.data.repository.UserSettingsRepository
import com.radarlabs.freegameradar.data.repository.UserStatsRepository
import com.radarlabs.freegameradar.ui.auth.AuthGate
import com.radarlabs.freegameradar.ui.components.BottomNavBar
import com.radarlabs.freegameradar.ui.components.TopBar
import com.radarlabs.freegameradar.ui.navigation.AppNavigation
import com.radarlabs.freegameradar.ui.navigation.Screen
import com.radarlabs.freegameradar.ui.theme.ModernDarkTheme
import com.radarlabs.freegameradar.ui.viewmodel.AdFreeViewModel
import com.radarlabs.freegameradar.ui.viewmodel.AuthViewModel
import com.radarlabs.freegameradar.ui.viewmodel.GameViewModel
import com.radarlabs.freegameradar.ui.viewmodel.NotificationViewModel
import com.radarlabs.freegameradar.ui.viewmodel.SettingsViewModel
import com.radarlabs.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.radarlabs.freegameradar.ui.viewmodel.UserStatsViewModel
import com.radarlabs.freegameradar.ui.viewmodel.SetupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    authViewModel: AuthViewModel,
    adFreeViewModel: AdFreeViewModel,
    authRepository: AuthRepository,
    userSettingsRepository: UserSettingsRepository,
    userStatsRepository: UserStatsRepository,
    startRoute: String? = null,
    onShowRefreshAd: () -> Unit = {},
    onShowSettingsAd: () -> Unit = {},
    onShowGameDetailAd: () -> Unit = {}
) {
    ModernDarkTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        var isBottomBarVisible by remember { mutableStateOf(true) }

        AppContainer { gameRepository, notificationRepository, _ ->
            val gameViewModel: GameViewModel = viewModel { GameViewModel() }
            val notificationViewModel: NotificationViewModel = viewModel { NotificationViewModel(notificationRepository) }
            val userStatsViewModel: UserStatsViewModel = viewModel { UserStatsViewModel(userStatsRepository, gameRepository) }
            val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(authRepository, userSettingsRepository) }
            val userPreferencesViewModel: UserPreferencesViewModel = viewModel { UserPreferencesViewModel(userSettingsRepository) }
            val setupViewModel: SetupViewModel = viewModel { SetupViewModel(userSettingsRepository, authRepository) }

            AuthGate(authViewModel = authViewModel) {
                Scaffold(
                    topBar = {
                        if (currentRoute != Screen.Setup.route && currentRoute != Screen.Gate.route) {
                            TopBar(navController, notificationViewModel)
                        }
                    },
                    bottomBar = {
                        AnimatedVisibility(
                            visible = isBottomBarVisible,
                            enter = slideInVertically(initialOffsetY = { it }) + expandVertically(expandFrom = Alignment.Bottom),
                            exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            if (currentRoute != Screen.Setup.route && currentRoute != Screen.Gate.route) {
                                BottomNavBar(navController, userPreferencesViewModel)
                            }
                        }
                    }
                ) { innerPadding ->
                    AppNavigation(
                        navController = navController,
                        innerPadding = innerPadding,
                        gameViewModel = gameViewModel,
                        userPreferencesViewModel = userPreferencesViewModel,
                        notificationViewModel = notificationViewModel,
                        userStatsViewModel = userStatsViewModel,
                        settingsViewModel = settingsViewModel,
                        setupViewModel = setupViewModel,
                        onBottomBarVisibilityChange = { isBottomBarVisible = it },
                        startRoute = startRoute,
                        onShowRefreshAd = onShowRefreshAd,
                        onShowSettingsAd = onShowSettingsAd,
                        onShowGameDetailAd = onShowGameDetailAd
                    )
                }
            }
        }
    }
}
