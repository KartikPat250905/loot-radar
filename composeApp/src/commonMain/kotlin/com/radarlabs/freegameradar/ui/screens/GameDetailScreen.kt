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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.radarlabs.freegameradar.data.GameDatabaseProvider
import com.radarlabs.freegameradar.data.mappers.toDto
import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.data.remote.ApiService
import com.radarlabs.freegameradar.data.repository.GameRepository
import com.radarlabs.freegameradar.ui.components.AppLoadingScreen
import com.radarlabs.freegameradar.ui.components.BackButton
import com.radarlabs.freegameradar.ui.components.GameWorth
import com.radarlabs.freegameradar.ui.viewmodel.UserStatsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import com.radarlabs.freegameradar.review.rememberReviewPrompter
import com.radarlabs.freegameradar.ui.viewmodel.ReviewViewModel
import com.radarlabs.freegameradar.util.ConnectivityObserver
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.datetime.*

@Composable
fun GameDetailScreen(
    navController: NavHostController,
    gameId: Long?,
    userStatsViewModel: UserStatsViewModel,
    reviewViewModel: ReviewViewModel,
    modifier: Modifier = Modifier,
    onShowGameDetailAd: () -> Unit = {}
) {
    var game by remember { mutableStateOf<GameDto?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isMissingFromLiveList by remember { mutableStateOf(false) }
    var fetchError by remember { mutableStateOf<String?>(null) }
    var retryCount by remember { mutableStateOf(0) }
    val repository = remember { GameRepository(ApiService()) }
    val uriHandler = LocalUriHandler.current
    var timeRemaining by remember { mutableStateOf<String?>(null) }
    val claimedGameIds by userStatsViewModel.claimedGameIds.collectAsState()
    val isClaimed = gameId in claimedGameIds

    val reviewPrompter = rememberReviewPrompter()
    val reviewState by reviewViewModel.uiState.collectAsState()
    val connectivityObserver = remember { ConnectivityObserver() }
    val isOnline by connectivityObserver.isOnline.collectAsState(initial = true)
    val isOnlineNow by rememberUpdatedState(isOnline)

    DisposableEffect(Unit) {
        onDispose {
            reviewViewModel.clearPendingPrompt()
        }
    }

    LaunchedEffect(reviewState.pendingPrompt) {
        if (!reviewState.pendingPrompt) return@LaunchedEffect
        if (!isOnlineNow || fetchError != null) {
            reviewViewModel.clearPendingPrompt()
            return@LaunchedEffect
        }
        reviewPrompter.requestReview()
        reviewViewModel.onPromptPresented()
    }

    // Load selected game based on ID
    LaunchedEffect(gameId, retryCount) {
        userStatsViewModel.onGameDetailView()
        if (gameId == null) {
            isLoading = false
            return@LaunchedEffect
        }

        fetchError = null
        isLoading = true

        // 1. Try loading cached copy from local DB first
        val cachedGame = withContext(Dispatchers.IO) {
            try {
                GameDatabaseProvider.getDatabase().gameQueries.selectById(gameId).executeAsOneOrNull()?.toDto()
            } catch (e: Exception) {
                null
            }
        }

        if (cachedGame != null) {
            game = cachedGame
            isLoading = false
        }

        // 2. Collect from repository flow (updates DB / live list)
        repository.getFreeGames()
            .catch { e ->
                println("Error fetching game detail: ${e.message}")
                if (game == null) {
                    fetchError = "Couldn't connect to the radar. Check your connection."
                }
                isLoading = false
            }
            .collect { list ->
                if (list.isNotEmpty()) {
                    val found = list.find { it.id == gameId }
                    if (found != null) {
                        game = found
                        isMissingFromLiveList = false
                    } else {
                        isMissingFromLiveList = true
                    }
                }
                isLoading = false
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

    val isGiveawayEnded = isMissingFromLiveList ||
            game?.status?.equals("Ended", ignoreCase = true) == true ||
            game?.status?.equals("Expired", ignoreCase = true) == true ||
            game?.status?.equals("Closed", ignoreCase = true) == true ||
            isExpired(game?.end_date)

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
        if (isLoading) {
            AppLoadingScreen(fullScreen = true)
        } else if (game == null && fetchError != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("📡", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Connection Error",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = fetchError!!,
                        fontSize = 15.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = { retryCount++ },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Retry", color = Color(0xFF0D1B2A), fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1B263B)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Go Back", color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else if (game == null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text("⏳", fontSize = 64.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Giveaway Ended",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "This giveaway is no longer available or has expired.",
                        fontSize = 15.sp,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1B263B)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Go Back", color = Color(0xFF6EE7B7), fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
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
                                            if (isGiveawayEnded) Color(0xFFEF4444) else Color(0xFF10B981),
                                            if (isGiveawayEnded) Color(0xFFF87171) else Color(0xFF34D399),
                                            if (isGiveawayEnded) Color(0xFFEF4444) else Color(0xFF10B981),
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
                            text = if (isGiveawayEnded) "Ended" else (g.status ?: "Unknown"),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isGiveawayEnded) Color(0xFFEF4444) else Color(0xFF10B981),
                            letterSpacing = 0.3.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Expiry Information Card
                        if (!g.end_date.isNullOrBlank()) {
                            val isExpiredDate = isExpired(g.end_date)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = if (isGiveawayEnded || isExpiredDate) {
                                                listOf(
                                                    Color(0xFFEF4444).copy(alpha = 0.15f),
                                                    Color(0xFFF87171).copy(alpha = 0.1f)
                                                )
                                            } else {
                                                listOf(
                                                    Color(0xFF10B981).copy(alpha = 0.15f),
                                                    Color(0xFF34D399).copy(alpha = 0.1f)
                                                )
                                            }
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
                                                    text = if (isGiveawayEnded || isExpiredDate) "⏰ Expired" else "⏰ Expires",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isGiveawayEnded || isExpiredDate) Color(0xFFF87171) else Color(0xFF6EE7B7),
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
                                                color = if (isGiveawayEnded || isExpiredDate) Color(0xFFF87171) else Color(0xFF6EE7B7),
                                                letterSpacing = 0.5.sp
                                            )
                                            Text(
                                                text = if (isGiveawayEnded) "Expired" else timeRemaining!!,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isGiveawayEnded || isExpiredDate || timeRemaining == "Expired") {
                                                    Color(0xFFEF4444)
                                                } else if (isExpiringSoon(g.end_date!!)) {
                                                    Color(0xFFF59E0B)
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
                                isEnded = isGiveawayEnded,
                                onClick = {
                                    if (!isClaimed && !isGiveawayEnded) {
                                        g.id?.let { id ->
                                            userStatsViewModel.addToClaimedValue(id, g.worth)
                                            reviewViewModel.onSuccessfulClaim()
                                        }
                                    }
                                    if (!isGiveawayEnded) {
                                        uriHandler.openUri(g.open_giveaway_url!!)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(80.dp))
                    }
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
    isEnded: Boolean = false,
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
                    brush = if (isEnded) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4B5563),
                                Color(0xFF374151)
                            )
                        )
                    } else if (isClaimed) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF059669),
                                Color(0xFF10B981),
                                Color(0xFF059669)
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
                        enabled = !isEnded,
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
                    if (isEnded) {
                        Text(
                            "🛑 Giveaway Ended",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF9CA3AF),
                            letterSpacing = 0.5.sp
                        )
                    } else if (isClaimed) {
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

// Helper function to check if end date is expired
fun isExpired(endDateStr: String?): Boolean {
    if (endDateStr.isNullOrBlank()) return false
    return try {
        val isoDateStr = endDateStr.replace(" ", "T")
        val finalDateStr = if (isoDateStr.endsWith("Z")) isoDateStr else "${isoDateStr}Z"
        val endInstant = Instant.parse(finalDateStr)
        val now = Clock.System.now()
        (endInstant - now).isNegative()
    } catch (e: Exception) {
        false
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

        val monthName = when (localDateTime.monthNumber) {
            1 -> "Jan"
            2 -> "Feb"
            3 -> "Mar"
            4 -> "Apr"
            5 -> "May"
            6 -> "Jun"
            7 -> "Jul"
            8 -> "Aug"
            9 -> "Sep"
            10 -> "Oct"
            11 -> "Nov"
            12 -> "Dec"
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
        !duration.isNegative() && duration.inWholeHours in 0..24
    } catch (e: Exception) {
        false
    }
}
