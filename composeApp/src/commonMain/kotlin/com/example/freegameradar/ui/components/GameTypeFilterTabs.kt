package com.example.freegameradar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.ui.viewmodel.GameTypeFilter

@Composable
fun GameTypeFilterTabs(
    selectedFilter: GameTypeFilter,
    onFilterSelected: (GameTypeFilter) -> Unit
) {
    val isDesktop = remember {
        System.getProperty("os.name")?.let { os ->
            os.contains("Windows", ignoreCase = true) ||
                    os.contains("Mac", ignoreCase = true) ||
                    os.contains("Linux", ignoreCase = true)
        } ?: false
    }

    if (isDesktop) {
        DesktopGameTypeFilterTabs(selectedFilter, onFilterSelected)
    } else {
        MobileGameTypeFilterTabs(selectedFilter, onFilterSelected)
    }
}

// Original mobile design with TabRow
@Composable
private fun MobileGameTypeFilterTabs(
    selectedFilter: GameTypeFilter,
    onFilterSelected: (GameTypeFilter) -> Unit
) {
    val filters = GameTypeFilter.values()

    TabRow(
        selectedTabIndex = filters.indexOf(selectedFilter),
        containerColor = Color(0xFF0D1B2A),
        contentColor = Color(0xFF6EE7B7),
        indicator = { tabPositions ->
            if (filters.indexOf(selectedFilter) < tabPositions.size) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[filters.indexOf(selectedFilter)])
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF10B981),
                                    Color(0xFF34D399),
                                    Color(0xFF10B981)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                        )
                )
            }
        },
        divider = {}
    ) {
        filters.forEach { filter ->
            Tab(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = filter.toDisplayString(),
                        fontWeight = if (filter == selectedFilter) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (filter == selectedFilter) Color(0xFF10B981) else Color(0xFF6B7280)
                    )
                }
            )
        }
    }
}

// Clean desktop design - no outer background, just floating buttons
@Composable
private fun DesktopGameTypeFilterTabs(
    selectedFilter: GameTypeFilter,
    onFilterSelected: (GameTypeFilter) -> Unit
) {
    val filters = GameTypeFilter.values()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        filters.forEach { filter ->
            DesktopFilterButton(
                filter = filter,
                isSelected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DesktopFilterButton(
    filter: GameTypeFilter,
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

    Box(
        modifier = modifier
            .height(48.dp)
    ) {
        // Glow effect for selected button
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
                            radius = 300f
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
            )
        }

        // Button with gradient border for selected state
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
                                Color(0xFF1B263B),
                                Color(0xFF0D1B2A)
                            )
                        )
                    },
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(if (isSelected) 1.5.dp else 1.dp)
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
                        shape = RoundedCornerShape(11.dp)
                    )
                    .clip(RoundedCornerShape(11.dp))
                    .clickable(
                        onClick = onClick,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Bottom accent line for selected button
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 6.dp)
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
                    text = filter.toDisplayString(),
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isSelected) Color(0xFF6EE7B7) else Color(0xFF6B7280),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}
