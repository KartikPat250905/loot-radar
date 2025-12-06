package com.example.lootradarkmp.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object Browse : Screen("browse", "Browse", Icons.Default.Search)
    object Notification : Screen("notification", "Notifications", Icons.Default.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Details : Screen("details", "Details", Icons.Default.Info)
}