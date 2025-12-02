package com.example.lootradar.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.lootradar.ui.navigation.Screen

@Composable
fun BottomNavBar(navController: NavController) {
    val screens = listOf(Screen.Home, Screen.Browse, Screen.Notification, Screen.Settings)
    val startDestination = Screen.Home
    var currentScreen by rememberSaveable { mutableStateOf(startDestination.route) }

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        screens.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = currentScreen == screen.route,
                onClick = {
                    currentScreen = screen.route
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
