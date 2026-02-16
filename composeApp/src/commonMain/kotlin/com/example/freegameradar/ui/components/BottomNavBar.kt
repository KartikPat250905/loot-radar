package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

    val screens = listOf(
        Screen.Home,
        Screen.HotDeals,
        Screen.Stats,
        Screen.Settings
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // Top gradient line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF10B981),
                            Color(0xFF34D399),
                            Color(0xFF10B981),
                            Color.Transparent
                        )
                    )
                )
        )

        NavigationBar(
            windowInsets = NavigationBarDefaults.windowInsets,
            containerColor = Color(0xFF0D1B2A),
            contentColor = Color(0xFF6EE7B7)
        ) {
            screens.forEach { screen ->
                val selected = currentRoute == screen.route

                NavigationBarItem(
                    icon = {
                        if (screen == Screen.Settings && showBadge) {
                            Box {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.label,
                                    tint = if (selected) Color(0xFF10B981) else Color(0xFF6B7280)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .background(Color(0xFFEF4444), CircleShape)
                                )
                            }
                        } else {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                tint = if (selected) Color(0xFF10B981) else Color(0xFF6B7280)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = screen.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = selected,
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
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280),
                        indicatorColor = Color(0xFF1B263B)
                    )
                )
            }
        }
    }
}