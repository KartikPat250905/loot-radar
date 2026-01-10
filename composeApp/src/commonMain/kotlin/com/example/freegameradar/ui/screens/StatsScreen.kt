package com.example.freegameradar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.freegameradar.core.Platform  // ✅ Import Platform
import com.example.freegameradar.ui.screens.stats.DesktopStatsScreen
import com.example.freegameradar.ui.screens.stats.MobileStatsScreen
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel

@Composable
fun StatsScreen(viewModel: UserStatsViewModel, modifier: Modifier = Modifier) {
    if (Platform.isDesktop) {
        DesktopStatsScreen(viewModel, modifier)
    } else {
        MobileStatsScreen(viewModel, modifier)
    }
}
