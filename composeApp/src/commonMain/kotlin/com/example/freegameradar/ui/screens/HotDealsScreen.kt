package com.example.freegameradar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.freegameradar.ui.screens.hotdeals.DesktopHotDealsScreen
import com.example.freegameradar.ui.screens.hotdeals.MobileHotDealsScreen

@Composable
fun HotDealsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val isDesktop = remember {
        System.getProperty("os.name")?.let { os ->
            os.contains("Windows", ignoreCase = true) ||
                    os.contains("Mac", ignoreCase = true) ||
                    os.contains("Linux", ignoreCase = true)
        } ?: false
    }

    if (isDesktop) {
        DesktopHotDealsScreen(navController, modifier)
    } else {
        MobileHotDealsScreen(navController, modifier)
    }
}
