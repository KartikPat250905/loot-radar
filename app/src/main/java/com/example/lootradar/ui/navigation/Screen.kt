package com.example.lootradar.ui.navigation

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {

    data object Home : Screen("home", Icons.Default.Home, "Home")

    data object Details : Screen("details", Icons.Default.Menu, "Details")

    data object Notification : Screen("notification", Icons.Default.Notifications, "Notifications")

    data object Settings : Screen("settings", Icons.Default.Settings, "Settings")

    data object Browse : Screen("browse", Icons.Default.Search, "Browse")
    // Later:
    // data object Details : Screen("details/{gameId}") {
    //     fun createRoute(gameId: String) = "details/$gameId"
    // }
}
