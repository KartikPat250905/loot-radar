package com.radarlabs.freegameradar.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Gate : Screen("gate", "Gate", Icons.Default.Shield) // The new starting point
    object Setup : Screen("setup", "Setup", Icons.Default.Build)
    object PostSetupCheck : Screen("post_setup_check", "Post-Setup Check", Icons.Default.Check)
    object Home : Screen("home", "Home", Icons.Default.Home)
    object HotDeals : Screen("hot_deals", "Hot Deals", Icons.Default.Star)
    object Notification : Screen("notification", "Notifications", Icons.Default.Notifications)
    object NotificationBenefits : Screen("notification_benefits", "Notification Benefits", Icons.Default.Notifications)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Stats : Screen("stats", "Stats", Icons.Default.Equalizer)
    object About : Screen("about", "About", Icons.Default.Info)
    object Details : Screen("details/{gameId}", "Game Details", Icons.Default.Info) {
        fun createRoute(id: Long?) = "details/$id"
    }
}
