package com.example.freegameradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.remote.ApiService
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.ui.components.BackButton
import com.example.freegameradar.ui.components.GameWorth
import com.example.freegameradar.ui.components.RemoteImage
import com.example.freegameradar.ui.viewmodel.UserStatsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun GameDetailScreen(
    navController: NavHostController,
    gameId: Long?,
    userStatsViewModel: UserStatsViewModel,
    modifier: Modifier = Modifier
) {
    var game by remember { mutableStateOf<GameDto?>(null) }
    val repository = remember { GameRepository(ApiService()) }
    val uriHandler = LocalUriHandler.current
    var timeRemaining by remember { mutableStateOf<String?>(null) }
    val claimedGameIds by userStatsViewModel.claimedGameIds.collectAsState()
    val isClaimed = gameId in claimedGameIds

    // Load selected game based on ID
    LaunchedEffect(gameId) {
        repository.getFreeGames().collect { list ->
            game = list.find { it.id == gameId }
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

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            game?.image?.let { imageUrl ->
                RemoteImage(
                    url = imageUrl,
                    contentDescription = game?.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            game?.let { g ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    // Title
                    Text(
                        text = g.title ?: "Unknown Game",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Worth
                    GameWorth(price = g.worth)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status
                    Text(
                        text = g.status ?: "Unknown",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Expiry Information Card
                    if (!g.end_date.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "⏰ Expires",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = formatEndDate(g.end_date!!),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }

                                if (timeRemaining != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    HorizontalDivider(color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.2f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Text(
                                        text = "Time Remaining",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = timeRemaining!!,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isExpiringSoon(g.end_date!!)) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.onSecondaryContainer
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Platforms
                    Text(
                        text = "Platforms: ${g.platforms ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = g.description ?: "No description available",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Instructions
                    if (!g.instructions.isNullOrBlank()) {
                        Text(
                            text = "How to get it",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = g.instructions!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Giveaway Button
                    if (!g.open_giveaway_url.isNullOrBlank()) {
                        Button(
                            onClick = { 
                                if (!isClaimed) {
                                    g.id?.let { id ->
                                        val value = g.worth?.replace("$", "")?.toFloatOrNull() ?: 0f
                                        if (value > 0f) {
                                            userStatsViewModel.addToClaimedValue(id, value)
                                        }
                                    }
                                }
                                uriHandler.openUri(g.open_giveaway_url!!)
                             },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isClaimed) MaterialTheme.colorScheme.secondaryContainer else ButtonDefaults.buttonColors().containerColor,
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            if (isClaimed) {
                                val greenColor = Color(0xFF4CAF50)
                                Icon(
                                    Icons.Default.Check, 
                                    contentDescription = "Claimed",
                                    tint = greenColor
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Game Claimed", color = greenColor)
                            } else {
                                Text("🎁 Claim Game")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
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

// Helper function to calculate time remaining
fun calculateTimeRemaining(endDateStr: String): String? {
    return try {
        // Many APIs return date as "2023-10-27 23:59:00" which doesn't adhere to ISO 8601 strict T separator
        // We replace space with T to make it ISO compliant for Instant.parse if needed
        val isoDateStr = endDateStr.replace(" ", "T")
        // Appending Z if timezone is missing, assuming UTC
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
        // Fallback or debug print
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
