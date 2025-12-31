package com.example.freegameradar.ui.components
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.freegameradar.ui.navigation.Screen

@Composable
fun BottomNavBar(navController: NavController) {
    val screens = listOf(Screen.Home, Screen.HotDeals, Screen.Stats, Screen.Settings)
    
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets
    ) {
        screens.forEach { screen ->
            val selected = currentRoute == screen.route
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        // Pop up to the start destination of the graph to avoid building up a large back stack
                        popUpTo(navController.graph.startDestinationId)
                        // Avoid multiple copies of the same destination when re-selecting the same item
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
