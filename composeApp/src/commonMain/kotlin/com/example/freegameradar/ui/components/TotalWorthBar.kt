package com.example.freegameradar.ui.components

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
import com.example.freegameradar.core.Platform
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.state.DataSource
import kotlin.math.round

@Composable
fun TotalWorthBar(
    games: List<GameDto>,
    dataSource: DataSource
) {
    if (Platform.isDesktop) {
        DesktopTotalWorthBar(games, dataSource)
    } else {
        MobileTotalWorthBar(games, dataSource)
    }
}

@Composable
private fun MobileTotalWorthBar(
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
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .scale(scale)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x4410B981),
                            Color.Transparent
                        ),
                        radius = 800f
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

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
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF10B981)
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
                            text = "YOUR FREE GAMES VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF6EE7B7),
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

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
                                    fontSize = 36.sp
                                )
                            ) {
                                append(formattedTotal)
                            }
                        }

                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.displaySmall
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val (label, color) = when (dataSource) {
                                DataSource.NETWORK -> "LIVE DATA" to Color(0xFF10B981)
                                DataSource.CACHE -> "CACHED DATA" to Color(0xFFF59E0B)
                            }

                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, shape = RoundedCornerShape(50))
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = color,
                                letterSpacing = 1.2.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DesktopTotalWorthBar(
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

    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = glowAlpha),
                            Color.Transparent
                        ),
                        radius = 1200f
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

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
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(1.5.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.4f),
                                Color(0xFF34D399).copy(alpha = 0.3f),
                                Color(0xFF10B981).copy(alpha = 0.4f)
                            )
                        ),
                        shape = RoundedCornerShape(15.dp)
                    )
                    .padding(1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1B263B),
                                    Color(0xFF0D1B2A)
                                )
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "YOUR FREE GAMES VALUE",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6EE7B7),
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val (label, color) = when (dataSource) {
                                    DataSource.NETWORK -> "LIVE DATA" to Color(0xFF10B981)
                                    DataSource.CACHE -> "CACHED DATA" to Color(0xFFF59E0B)
                                }

                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(color, shape = RoundedCornerShape(50))
                                )

                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = color,
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                            }
                        }

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
                                    fontSize = 32.sp
                                )
                            ) {
                                append(formattedTotal)
                            }
                        }

                        Text(
                            text = priceText,
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF10B981).copy(alpha = 0.5f),
                                        Color(0xFF34D399).copy(alpha = 0.6f),
                                        Color(0xFF10B981).copy(alpha = 0.5f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }
        }
    }
}