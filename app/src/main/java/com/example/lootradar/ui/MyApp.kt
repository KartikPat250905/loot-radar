package com.example.lootradar.ui
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.lootradar.ui.components.BottomNavBar
import androidx.compose.material3.Scaffold
import com.example.lootradar.ui.components.TopBar
import com.example.lootradar.ui.navigation.AppNavigation
import com.example.todoapp.ui.theme.ModernDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyApp()
{
    val navController = rememberNavController()
    ModernDarkTheme {
        Scaffold(
            topBar = {TopBar(navController)},
            bottomBar = {BottomNavBar(navController)}
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                innerPadding = innerPadding
            )
        }
    }
}