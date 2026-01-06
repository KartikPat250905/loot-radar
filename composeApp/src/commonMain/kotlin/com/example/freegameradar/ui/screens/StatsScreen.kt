package com.example.freegameradar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.freegameradar.ui.screens.stats.DesktopStatsScreen
import com.example.freegameradar.ui.screens.stats.MobileStatsScreen
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel

@Composable
fun StatsScreen(viewModel: UserStatsViewModel, modifier: Modifier = Modifier) {
    val isDesktop = remember {
        System.getProperty("os.name")?.let { os ->
            os.contains("Windows", ignoreCase = true) ||
                    os.contains("Mac", ignoreCase = true) ||
                    os.contains("Linux", ignoreCase = true)
        } ?: false
    }

    if (isDesktop) {
        DesktopStatsScreen(viewModel, modifier)
    } else {
        MobileStatsScreen(viewModel, modifier)
    }
}
