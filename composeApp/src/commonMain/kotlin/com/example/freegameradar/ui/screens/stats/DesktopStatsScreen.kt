package com.example.freegameradar.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.ui.components.PlatformStatsCard
import com.example.freegameradar.ui.components.TotalClaimedBar
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel

@Composable
fun DesktopStatsScreen(viewModel: UserStatsViewModel, modifier: Modifier = Modifier) {
    val platformStats by viewModel.platformStats.collectAsState()
    val totalClaimedValue by viewModel.totalClaimedValue.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B),
                        Color(0xFF0D1B2A)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = "Live Game Statistics",
            fontSize = 36.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE5E7EB),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        TotalClaimedBar(claimedValue = totalClaimedValue)

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            contentAlignment = Alignment.Center
        ) {
            PlatformStatsCard(platformStats = platformStats)
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
