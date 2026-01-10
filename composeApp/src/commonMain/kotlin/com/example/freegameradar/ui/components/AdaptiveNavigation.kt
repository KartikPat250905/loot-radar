package com.example.freegameradar.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.core.Platform  // ✅ ADD THIS IMPORT at top

@Composable
fun AdaptiveNavigationBar(navController: NavController) {
    // ✅ Use Platform.isDesktop instead of System.getProperty
    if (Platform.isDesktop) {
        DesktopNavigationRail(navController)
    } else {
        MobileBottomNavBar(navController)
    }
}


// Mobile Bottom Navigation
@Composable
private fun MobileBottomNavBar(navController: NavController) {
    val screens = listOf(Screen.Home, Screen.HotDeals, Screen.Stats, Screen.Settings)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Box(modifier = Modifier.fillMaxWidth()) {
        // Top gradient line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .align(Alignment.TopCenter)
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

        NavigationBar(
            windowInsets = NavigationBarDefaults.windowInsets,
            containerColor = Color(0xFF0D1B2A),
            contentColor = Color(0xFF6EE7B7)
        ) {
            screens.forEach { screen ->
                val selected = currentRoute == screen.route
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector = screen.icon,
                            contentDescription = screen.label,
                            tint = if (selected) Color(0xFF10B981) else Color(0xFF6B7280)
                        )
                    },
                    label = {
                        Text(
                            text = screen.label,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    selected = selected,
                    onClick = {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF10B981),
                        selectedTextColor = Color(0xFF10B981),
                        unselectedIconColor = Color(0xFF6B7280),
                        unselectedTextColor = Color(0xFF6B7280),
                        indicatorColor = Color(0xFF1B263B)
                    )
                )
            }
        }
    }
}

// Modern Desktop Navigation Rail with Animated Logo
@Composable
private fun DesktopNavigationRail(navController: NavController) {
    val screens = listOf(Screen.Home, Screen.HotDeals, Screen.Stats, Screen.Settings)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(90.dp),
        color = Color(0xFF0D1B2A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated Cyberpunk Logo
            ThemedLogoButton(
                onClick = {
                    // Navigate to home when logo is clicked
                    navController.navigate(Screen.Home.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Navigation Items
            screens.forEach { screen ->
                val selected = currentRoute == screen.route

                Box(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                ) {
                    Surface(
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = if (selected) Color(0xFF1B263B) else Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.label,
                                tint = if (selected) Color(0xFF10B981) else Color(0xFF6B7280),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = screen.label,
                                fontSize = 10.sp,
                                color = if (selected) Color(0xFF10B981) else Color(0xFF6B7280),
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1
                            )
                        }
                    }

                    // Selection indicator
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .width(4.dp)
                                .height(32.dp)
                                .clip(RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp))
                                .background(Color(0xFF10B981))
                        )
                    }
                }
            }
        }
    }
}

// Animated Logo Component
@Composable
private fun ThemedLogoButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .size(56.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1B263B),
                        Color(0xFF0D1B2A)
                    )
                )
            )
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF10B981).copy(alpha = glowAlpha),
                            Color.Transparent
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(2.dp)
                .align(Alignment.BottomCenter)
                .offset(y = (-8).dp)
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

        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🎮",
                fontSize = 28.sp
            )
        }
    }
}
