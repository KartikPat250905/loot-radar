package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.example.freegameradar.util.isNotificationPermissionGranted

@Composable
fun BottomNavBar(
    navController: NavHostController,
    userPreferencesViewModel: UserPreferencesViewModel
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val preferencesState by userPreferencesViewModel.uiState.collectAsState()
    val permissionState = isNotificationPermissionGranted()

    val showBadge = preferencesState.notificationsEnabled && !permissionState

    val items = listOf(
        Screen.Home,
        Screen.Notification,
        Screen.Stats,
        Screen.Settings
    )

    NavigationBar(
        containerColor = Color(0xFF1B263B),
        contentColor = Color(0xFF60A5FA)
    ) {
        items.forEach { screen ->
            val isSelected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    val icon = when (screen) {
                        Screen.Home -> Icons.Filled.Home
                        Screen.Notification -> Icons.Filled.Notifications
                        Screen.Stats -> Icons.Filled.QueryStats
                        Screen.Settings -> Icons.Filled.Settings
                        else -> Icons.Filled.Home
                    }

                    if (screen == Screen.Settings && showBadge) {
                        Box {
                            Icon(icon, contentDescription = screen.route)
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .align(Alignment.TopEnd)
                                    .offset(x = 4.dp, y = (-4).dp)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                    } else {
                        Icon(icon, contentDescription = screen.route)
                    }
                },
                label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                selected = isSelected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF60A5FA),
                    selectedTextColor = Color(0xFF60A5FA),
                    unselectedIconColor = Color(0xFF9CA3AF),
                    unselectedTextColor = Color(0xFF9CA3AF),
                    indicatorColor = Color(0xFF1E3A5F)
                )
            )
        }
    }
}
