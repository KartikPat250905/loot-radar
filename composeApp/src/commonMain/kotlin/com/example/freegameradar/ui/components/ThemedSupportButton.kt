package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ThemedSupportButton(
    text: String,
    onClick: () -> Unit,
    icon: ImageVector? = null,
    isPrimary: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isPrimary) {
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
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(if (isPrimary) 2.dp else 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF1B263B),
                                Color(0xFF0D1B2A)
                            )
                        ),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    icon?.let {
                        Icon(
                            imageVector = it,
                            contentDescription = null,
                            tint = if (isPrimary) Color(0xFF6EE7B7) else Color(0xFF9CA3AF),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        text = text,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = if (isPrimary) Color(0xFF6EE7B7) else Color(0xFF9CA3AF),
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}