package com.radarlabs.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.data.remote.ApiService
import com.radarlabs.freegameradar.data.repository.GameRepository
import com.radarlabs.freegameradar.ui.components.BackButton
import com.radarlabs.freegameradar.ui.components.GameWorth
import com.radarlabs.freegameradar.ui.viewmodel.UserStatsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.datetime.*

@Composable
fun GameDetailScreen(
    navController: NavHostController,
    gameId: Long?,
    userStatsViewModel: UserStatsViewModel,
    modifier: Modifier = Modifier,
    onShowGameDetailAd: () -> Unit = {}
) {
    var game by remember { mutableStateOf<GameDto?>(null) }
    val repository = remember { GameRepository(ApiService()) }
    val uriHandler = LocalUriHandler.current
    var timeRemaining by remember { mutableStateOf<String?>(null) }
    val claimedGameIds by userStatsViewModel.claimedGameIds.collectAsState()
    val isClaimed = gameId in claimedGameIds

    // Load selected game based on ID
    LaunchedEffect(gameId) {
        userStatsViewModel.onGameDetailView()
        repository.getFreeGames().collect { list ->
            game = list.find { it.id == gameId }
        }
    }

    LaunchedEffect(Unit) {
        userStatsViewModel.showGameDetailAd.collectLatest {
            onShowGameDetailAd()
        }
    }

    // Update countdown timer every second
    LaunchedEffect(game) {
        if (game?.end_date != null) {
            while (isActive) {
                timeRemaining = calculateTimeRemaining(game?.end_date!!)
                delay(1000)
            }
        }
    }

    Box(
        modifier = modifier
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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Image with gradient overlay
            game?.image?.let { imageUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = game?.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )

                    // Gradient overlay
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0x40000000),
                                        Color(0xCC0D1B2A)
                                    )
                                )
                            )
                    )

                    // Bottom glow
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
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            game?.let { g ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    // Title
                    Text(
                        text = g.title ?: "Unknown Game",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE5E7EB),
                        letterSpacing = 0.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Worth
                    GameWorth(price = g.worth)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status
                    Text(
                        text = g.status ?: "Unknown",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF10B981),
                        letterSpacing = 0.3.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Expiry Information Card
                    if (!g.end_date.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF10B981).copy(alpha = 0.15f),
                                            Color(0xFF34D399).copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(1.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        color = Color(0xFF1B263B),
                                        shape = RoundedCornerShape(15.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "⏰ Expires",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF6EE7B7),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = formatEndDate(g.end_date!!),
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF9CA3AF)
                                            )
                                        }
                                    }

                                    if (timeRemaining != null) {
                                        Spacer(modifier = Modifier.height(12.dp))

                                        HorizontalDivider(
                                            color = Color(0xFF374151),
                                            thickness = 1.dp
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = "Time Remaining",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF6EE7B7),
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = timeRemaining!!,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isExpiringSoon(g.end_date!!)) {
                                                Color(0xFFEF4444)
                                            } else {
                                                Color(0xFF10B981)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Platforms
                    Text(
                        text = "Platforms",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6EE7B7),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = g.platforms ?: "Unknown",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Description
                    Text(
                        text = "About",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5E7EB),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = g.description ?: "No description available",
                        fontSize = 14.sp,
                        color = Color(0xFF9CA3AF),
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Instructions
                    if (!g.instructions.isNullOrBlank()) {
                        Text(
                            text = "How to Claim",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE5E7EB),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFF1B263B).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = g.instructions!!,
                                fontSize = 14.sp,
                                color = Color(0xFF9CA3AF),
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Claim Button
                    if (!g.open_giveaway_url.isNullOrBlank()) {
                        ThemedClaimButton(
                            isClaimed = isClaimed,
                            onClick = {
                                if (!isClaimed) {
                                    g.id?.let { id ->
                                        // Pass the worth string directly - ViewModel will handle parsing
                                        userStatsViewModel.addToClaimedValue(id, g.worth)
                                    }
                                }
                                uriHandler.openUri(g.open_giveaway_url!!)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Styled Floating Back Button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            BackButton(onClick = { navController.popBackStack() })
        }
    }
}

@Composable
private fun ThemedClaimButton(
    isClaimed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isClaimed) {
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
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF10B981)
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(2.dp)
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
                    if (isClaimed) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Claimed",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Game Claimed",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF10B981),
                            letterSpacing = 0.5.sp
                        )
                    } else {
                        Text(
                            "🎁 Claim Game",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF6EE7B7),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

// Helper function to calculate time remaining
fun calculateTimeRemaining(endDateStr: String): String? {
    return try {
        val isoDateStr = endDateStr.replace(" ", "T")
        val finalDateStr = if (isoDateStr.endsWith("Z")) isoDateStr else "${isoDateStr}Z"

        val endInstant = Instant.parse(finalDateStr)
        val now = Clock.System.now()
        val duration = endInstant - now

        if (duration.isNegative()) {
            "Expired"
        } else {
            val days = duration.inWholeDays
            val hours = duration.inWholeHours % 24
            val minutes = duration.inWholeMinutes % 60
            val seconds = duration.inWholeSeconds % 60

            when {
                days > 0 -> "${days}d ${hours}h ${minutes}m ${seconds}s"
                hours > 0 -> "${hours}h ${minutes}m ${seconds}s"
                minutes > 0 -> "${minutes}m ${seconds}s"
                else -> "${seconds}s"
            }
        }
    } catch (e: Exception) {
        println("Error parsing date: $endDateStr -> ${e.message}")
        null
    }
}

// Helper function to format the end date
fun formatEndDate(endDateStr: String): String {
    return try {
        val isoDateStr = endDateStr.replace(" ", "T")
        val finalDateStr = if (isoDateStr.endsWith("Z")) isoDateStr else "${isoDateStr}Z"

        val instant = Instant.parse(finalDateStr)
        val localDateTime = instant.toLocalDateTime(TimeZone.UTC)

        val monthName = when (localDateTime.month) {
            Month.JANUARY -> "Jan"
            Month.FEBRUARY -> "Feb"
            Month.MARCH -> "Mar"
            Month.APRIL -> "Apr"
            Month.MAY -> "May"
            Month.JUNE -> "Jun"
            Month.JULY -> "Jul"
            Month.AUGUST -> "Aug"
            Month.SEPTEMBER -> "Sep"
            Month.OCTOBER -> "Oct"
            Month.NOVEMBER -> "Nov"
            Month.DECEMBER -> "Dec"
            else -> "Unknown"
        }

        val hour = localDateTime.hour.toString().padStart(2, '0')
        val minute = localDateTime.minute.toString().padStart(2, '0')

        "$monthName ${localDateTime.dayOfMonth}, ${localDateTime.year} at $hour:$minute UTC"
    } catch (e: Exception) {
        endDateStr
    }
}

// Helper function to check if expiring within 24 hours
fun isExpiringSoon(endDateStr: String): Boolean {
    return try {
        val isoDateStr = endDateStr.replace(" ", "T")
        val finalDateStr = if (isoDateStr.endsWith("Z")) isoDateStr else "${isoDateStr}Z"

        val endInstant = Instant.parse(finalDateStr)
        val now = Clock.System.now()
        val duration = endInstant - now
        duration.inWholeHours in 0..24
    } catch (e: Exception) {
        false
    }
}
