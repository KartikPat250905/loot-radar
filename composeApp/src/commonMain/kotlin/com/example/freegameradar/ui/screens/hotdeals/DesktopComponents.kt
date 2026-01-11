package com.example.freegameradar.ui.screens.hotdeals

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.freegameradar.data.models.GameDto

@Composable
fun DesktopHeroBanner(
    game: GameDto?,
    isDragging: Boolean = false,
    onClick: () -> Unit
) {
    if (game == null) return

    DisableSelection {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp)
                .clickable(enabled = !isDragging) { onClick() },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B263B)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = game.image,
                    contentDescription = game.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp))
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0x20000000),
                                    Color(0x80000000),
                                    Color(0xCC0D1B2A)
                                )
                            )
                        )
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF10B981),
                                    Color(0xFF34D399),
                                    Color(0xFF10B981),
                                    Color.Transparent
                                )
                            )
                        )
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(32.dp)
                ) {
                    Text(
                        text = game.title ?: "",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE5E7EB),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = game.worth ?: "Free",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}

@Composable
fun DesktopTabButton(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    DisableSelection {
        Box(
            modifier = modifier.height(56.dp)
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = glowAlpha * 0.4f),
                                    Color.Transparent
                                ),
                                radius = 400f
                            ),
                            shape = RoundedCornerShape(14.dp)
                        )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isSelected) {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF10B981),
                                    Color(0xFF34D399),
                                    Color(0xFF10B981)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF374151).copy(alpha = 0.3f),
                                    Color(0xFF1F2937).copy(alpha = 0.3f)
                                )
                            )
                        },
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(if (isSelected) 2.dp else 1.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF1B263B),
                                        Color(0xFF0D1B2A)
                                    )
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF0D1B2A),
                                        Color(0xFF1B263B)
                                    )
                                )
                            },
                            shape = RoundedCornerShape(13.dp)
                        )
                        .clip(RoundedCornerShape(13.dp))
                        .clickable(
                            onClick = onClick,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(2.dp)
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF10B981).copy(alpha = 0.6f),
                                            Color(0xFF34D399).copy(alpha = 0.8f),
                                            Color(0xFF10B981).copy(alpha = 0.6f),
                                            Color.Transparent
                                        )
                                    )
                                )
                        )
                    }

                    Text(
                        text = title,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = if (isSelected) Color(0xFF6EE7B7) else Color(0xFF6B7280),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
