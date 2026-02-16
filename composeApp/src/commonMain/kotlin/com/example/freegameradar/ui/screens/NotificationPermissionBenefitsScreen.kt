package com.radarlabs.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NotificationPermissionBenefitsScreen(
    onEnableNotifications: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
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
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Animated Radar Icon with gradient
        Box(
            modifier = Modifier.size(120.dp)
        ) {
            // Outer glow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF10B981).copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = CircleShape
                    )
            )
            
            // Inner circle with gradient border
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.Center)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF10B981)
                            )
                        ),
                        shape = CircleShape
                    )
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = Color(0xFF1B263B),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                        tint = Color(0xFF10B981)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Headline with gradient
        val headlineText = buildAnnotatedString {
            append("Don't Miss Out! ")
            withStyle(
                style = SpanStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF10B981),
                            Color(0xFF34D399),
                            Color(0xFF6EE7B7)
                        )
                    )
                )
            ) {
                append("🎯")
            }
        }
        
        Text(
            text = headlineText,
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE5E7EB),
            textAlign = TextAlign.Center,
            letterSpacing = 0.5.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Get instant alerts when epic games go FREE",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center,
            letterSpacing = 0.3.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Benefits List
        BenefitItem(
            emoji = "⚡",
            title = "Lightning Fast Alerts",
            description = "Be the FIRST to know when premium games drop to $0. No more missed opportunities!"
        )

        BenefitItem(
            emoji = "💎",
            title = "Claim Before They're Gone",
            description = "Limited-time free games disappear fast. We'''ll notify you the second they'''re available."
        )

        BenefitItem(
            emoji = "🎮",
            title = "Never Pay Full Price Again",
            description = "Epic, Steam, GOG, and more — catch every freebie across all platforms. Your wallet will thank you."
        )

        BenefitItem(
            emoji = "🔕",
            title = "Smart & Silent",
            description = "Only get notified about games YOU want. No spam, no noise, just quality freebies."
        )

        BenefitItem(
            emoji = "📊",
            title = "Build Your Library",
            description = "Average users save $7000 - $8000/year in free games. That'''s multiple AAA titles you didn'''t buy!"
        )

        Spacer(modifier = Modifier.height(40.dp))

        // CTA Button - Themed
        ThemedNotificationButton(
            text = "Enable Notifications 🔔",
            onClick = onEnableNotifications,
            isPrimary = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Skip Button - Styled link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    onClick = onSkip,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                )
                .padding(12.dp)
        ) {
            Text(
                text = "Maybe later",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6B7280),
                letterSpacing = 0.3.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer Note
        Text(
            text = "You can customize notification preferences anytime in Settings",
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun BenefitItem(
    emoji: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Emoji Box with gradient border
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.3f),
                            Color(0xFF34D399).copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color(0xFF1B263B),
                        shape = RoundedCornerShape(13.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = emoji,
                    fontSize = 28.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Text Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE5E7EB),
                letterSpacing = 0.3.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9CA3AF),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ThemedNotificationButton(
    text: String,
    onClick: () -> Unit,
    isPrimary: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(56.dp)
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
                Text(
                    text = text,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF6EE7B7),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}