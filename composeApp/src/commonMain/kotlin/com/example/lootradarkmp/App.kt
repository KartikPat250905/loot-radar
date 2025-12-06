package com.example.lootradarkmp

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.lootradarkmp.ui.components.BottomNavBar
import com.example.lootradarkmp.ui.components.TopBar
import com.example.lootradarkmp.ui.navigation.AppNavigation
import com.example.lootradarkmp.ui.theme.ModernDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val navController = rememberNavController()
    ModernDarkTheme {
        Scaffold(
            topBar = { TopBar(navController) },
            bottomBar = { BottomNavBar(navController) }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                innerPadding = innerPadding
            )
        }
    }
}