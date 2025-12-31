package com.example.freegameradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.freegameradar.ui.components.PlatformStatsCard
import com.example.freegameradar.ui.components.TotalClaimedBar
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(viewModel: UserStatsViewModel, modifier: Modifier = Modifier) {
    val claimedValue by viewModel.claimedValue.collectAsState()
    val platformStats by viewModel.platformStats.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Game Stats") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            TotalClaimedBar(claimedValue = claimedValue)
            PlatformStatsCard(platformStats = platformStats)
        }
    }
}