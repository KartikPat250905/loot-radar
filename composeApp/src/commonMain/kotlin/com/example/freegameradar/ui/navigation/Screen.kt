package com.example.freegameradar.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Home : Screen("home", "Home", Icons.Default.Home)
    object HotDeals : Screen("hot_deals", "Hot Deals", Icons.Default.Star)
    object Notification : Screen("notification", "Notifications", Icons.Default.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Details : Screen("details/{gameId}", "Game Details", Icons.Default.Info) {
        fun createRoute(id: Long?) = "details/$id"
    }
}
