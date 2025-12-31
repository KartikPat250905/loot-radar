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

@Composable
fun TotalClaimedBar(
    claimedValue: Float
) {
    val formattedTotal = "$${String.format("%.2f", claimedValue)}"

    // Pulse animation for the entire card
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
        // Outer glow effect
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0x44FF6B35), // Orange glow
                            Color.Transparent
                        ),
                        radius = 800f
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
        )

        // Main card with gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0D1B2A), // Deep dark blue-gray
                            Color(0xFF1B263B), // Slightly lighter dark
                            Color(0xFF0D1B2A)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(1.5.dp) // Border width
        ) {
            // Inner gradient border effect
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B35), // Vibrant orange
                                Color(0xFFF7931E), // Golden orange
                                Color(0xFFFF6B35)
                            )
                        ),
                        shape = RoundedCornerShape(19.dp)
                    )
                    .padding(1.5.dp)
            ) {
                // Content background
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
                            text = "TOTAL CLAIMED VALUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDB777),
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Price display with gradient
                        val priceText = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFFFF6B35),
                                            Color(0xFFF7931E),
                                            Color(0xFFFDB777)
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

                        Text(
                            text = "Money saved on claimed games",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFDB777).copy(alpha = 0.8f),
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}