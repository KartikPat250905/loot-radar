// ui/components/RefreshButton.kt
package com.example.freegameradar.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RefreshButton(
    isRefreshing: Boolean,
    canRefresh: Boolean,
    remainingSeconds: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showText: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refresh_rotation"
    )

    // ✅ FIXED: Simplified structure without nested Boxes
    Box(
        modifier = modifier
            .wrapContentSize()  // ✅ Wrap content instead of fill
            .background(
                brush = if (canRefresh && !isRefreshing) {
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
                            Color(0xFF374151).copy(alpha = 0.5f),
                            Color(0xFF1F2937).copy(alpha = 0.5f)
                        )
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(2.dp)  // Border thickness
            .clip(RoundedCornerShape(10.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B263B),
                        Color(0xFF0D1B2A)
                    )
                )
            )
            .clickable(
                enabled = canRefresh && !isRefreshing,
                onClick = {
                    println("🖱️ Refresh clicked: canRefresh=$canRefresh, isRefreshing=$isRefreshing, remaining=${remainingSeconds}s")
                    onClick()
                },
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),  // Internal padding
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedContent(
                targetState = when {
                    isRefreshing -> "refreshing"
                    !canRefresh -> "cooldown"
                    else -> "ready"
                },
                label = "refresh_state"
            ) { state ->
                when (state) {
                    "refreshing" -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF10B981)
                        )
                    }
                    "cooldown" -> {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh cooldown",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF6B7280)
                        )
                    }
                    else -> {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier
                                .size(20.dp)
                                .rotate(if (isRefreshing) rotation else 0f),
                            tint = Color(0xFF10B981)
                        )
                    }
                }
            }

            if (showText && !canRefresh && remainingSeconds > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Wait ${remainingSeconds}s",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
