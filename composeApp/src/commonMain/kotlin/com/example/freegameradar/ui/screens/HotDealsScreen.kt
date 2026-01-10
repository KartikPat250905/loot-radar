package com.example.freegameradar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.freegameradar.core.Platform
import com.example.freegameradar.ui.screens.hotdeals.DesktopHotDealsScreen
import com.example.freegameradar.ui.screens.hotdeals.MobileHotDealsScreen

@Composable
fun HotDealsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    if (Platform.isDesktop) {
        DesktopHotDealsScreen(navController, modifier)
    } else {
        MobileHotDealsScreen(navController, modifier)
    }
}
