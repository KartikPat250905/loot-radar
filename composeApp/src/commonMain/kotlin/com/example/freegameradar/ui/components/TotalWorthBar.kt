package com.radarlabs.freegameradar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.data.state.DataSource
import kotlin.math.round

@Composable
fun TotalWorthBar(
    games: List<GameDto>,
    dataSource: DataSource
) {
    val total = games.mapNotNull { game ->
        val priceStr = game.worth?.replace("$", "")?.replace(",", "")?.trim()
        when {
            priceStr == null -> 0.0
            priceStr.equals("N/A", ignoreCase = true) -> 0.0
            else -> priceStr.toDoubleOrNull() ?: 0.0
        }
    }.sum()

    val formattedTotal = "$${round(total * 100) / 100}"

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp) // ⬇️ was vertical = 12.dp
            .scale(scale)
    ) {
        // Outer glow — height reduced
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp) // ⬇️ was 90.dp
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x4410B981), Color.Transparent),
                        radius = 800f
                    ),
                    shape = RoundedCornerShape(16.dp) // ⬇️ was 20.dp
                )
        )

        // Main card
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
                    shape = RoundedCornerShape(16.dp) // ⬇️ was 20.dp
                )
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF10B981)
                            )
                        ),
                        shape = RoundedCornerShape(15.dp) // ⬇️ was 19.dp
                    )
                    .padding(1.5.dp)
            ) {
                // Content background
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF1B263B), Color(0xFF0D1B2A))
                            ),
                            shape = RoundedCornerShape(14.dp) // ⬇️ was 18.dp
                        )
                        .padding(horizontal = 20.dp, vertical = 8.dp), // ⬇️ was vertical = 16.dp
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left: label + data source badge
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "FREE GAMES VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7),
                                letterSpacing = 1.5.sp, // ⬇️ was 2.sp
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(3.dp)) // ⬇️ was 8.dp

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val (label, color) = when (dataSource) {
                                    DataSource.NETWORK -> "LIVE" to Color(0xFF10B981)
                                    DataSource.CACHE -> "CACHED" to Color(0xFFF59E0B)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp) // ⬇️ was 8.dp
                                        .background(color, shape = RoundedCornerShape(50))
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Right: price (big number, right-aligned)
                        val priceText = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF10B981),
                                            Color(0xFF34D399),
                                            Color(0xFF6EE7B7)
                                        )
                                    ),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 28.sp // ⬇️ was 36.sp
                                )
                            ) {
                                append(formattedTotal)
                            }
                        }

                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
            }
        }
    }
}
