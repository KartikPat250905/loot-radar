package com.example.freegameradar.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.model.PlatformStat

@Composable
fun PlatformStatsCard(platformStats: List<PlatformStat>) {
    val platformColors = remember {
        mapOf(
            "Steam" to Color(0xFF1B2838),
            "Epic Games" to Color(0xFFFFFFFF),
            "GOG" to Color(0xFF86328A),
            "Itch.io" to Color(0xFFFA5C5C),
            "Ubisoft" to Color(0xFF0080C7),
            "DRM-Free" to Color(0xFF4CAF50),
            "PC" to Color(0xFF5C7CFA),
            "Android" to Color(0xFF3DDC84),
            "iOS" to Color(0xFFA2AAAD),
            "PlayStation" to Color(0xFF003791),
            "Xbox" to Color(0xFF107C10),
            "Nintendo Switch" to Color(0xFFE60012),
            "EA Origin" to Color(0xFFFF6C2C),
            "Battle.net" to Color(0xFF148EFF),
            "VR" to Color(0xFF9C27B0),
            "Other" to Color(0xFF9E9E9E)
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
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "CURRENT FREE GAMES",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDB777),
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Available by Platform",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB0B8C1),
                            fontSize = 13.sp
                        )

                        if (platformStats.isEmpty()) {
                            Text(
                                text = "No games available yet.",
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        } else {
                            // Pie Chart with proper spacing
                            Box(
                                modifier = Modifier
                                    .size(220.dp)
                                    .padding(10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                PieChartCanvas(
                                    data = platformStats,
                                    colors = platformColors,
                                    total = total
                                )

                                // Center text
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "$total",
                                        color = Color.White,
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Games",
                                        color = Color(0xFFFDB777),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Legend with platform names
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                platformStats.forEach { stat ->
                                    PlatformLegendItem(
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
private fun PieChartCanvas(
    data: List<PlatformStat>,
    colors: Map<String, Color>,
    total: Int
) {
    Canvas(modifier = Modifier.size(200.dp)) {
        val canvasSize = size.minDimension
        val radius = canvasSize / 2f
        val strokeWidth = radius * 0.4f

        var startAngle = -90f

        data.forEach { stat ->
            val sweepAngle = (stat.count.toFloat() / total) * 360f
            val color = colors[stat.platform] ?: Color.Gray

            // Draw colored arc segment
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                size = Size(canvasSize, canvasSize),
                style = Stroke(width = strokeWidth)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun PlatformLegendItem(
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
                    .size(16.dp)
                    .background(color, CircleShape)
            )
            Text(
                text = platform,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "$count (${String.format("%.1f", percentage)}%)",
            color = Color(0xFFFDB777),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}