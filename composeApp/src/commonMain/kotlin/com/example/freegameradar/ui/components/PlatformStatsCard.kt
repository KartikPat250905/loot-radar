package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.model.PlatformStat

@Composable
fun PlatformStatsCard(platformStats: List<PlatformStat>) {
    val platformColors = remember {
        mapOf(
            "Steam" to Color(0xFF1B2838),
            "Epic Games" to Color(0xFF2A2A2A),
            "GOG" to Color(0xFF86328A),
            "Itch.io" to Color(0xFFFA5C5C),
            "Ubisoft" to Color(0xFF0080C7),
            "Other" to Color(0xFF4CAF50)
        )
    }

    val total = remember(platformStats) {
        platformStats.sumOf { it.count }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A),
                            Color(0xFF1B263B),
                            Color(0xFF0D1B2A)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B35),
                                Color(0xFFF7931E),
                                Color(0xFFFF6B35)
                            )
                        ),
                        shape = RoundedCornerShape(19.dp)
                    )
                    .padding(1.5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1B263B),
                                    Color(0xFF0D1B2A)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "PLATFORM STATS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDB777),
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (platformStats.isEmpty()) {
                            Text("No stats available yet.", color = Color.White)
                        } else {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                platformStats.forEach { stat ->
                                    PlatformStatItem(
                                        platform = stat.platform,
                                        count = stat.count,
                                        percentage = (stat.count.toFloat() / total * 100),
                                        color = platformColors[stat.platform] ?: Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlatformStatItem(
    platform: String,
    count: Int,
    percentage: Float,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = platform,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "$count (${String.format("%.1f", percentage)}%)",
            color = Color(0xFFFDB777),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}