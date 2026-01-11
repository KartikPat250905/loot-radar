package com.example.freegameradar.ui.screens

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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
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
    modifier: Modifier = Modifier
) {
    var game by remember { mutableStateOf<GameDto?>(null) }
    val repository = remember { GameRepository(ApiService()) }
    val uriHandler = LocalUriHandler.current
    var timeRemaining by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(gameId) {
        if (gameId != null) {
            game = repository.getGameById(gameId).firstOrNull()
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

                    Text(
                        text = g.title ?: "Unknown Game",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    GameWorth(price = g.worth)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = g.status ?: "Unknown",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

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

                    Text(
                        text = "Platforms: ${g.platforms ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = g.description ?: "No description available",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

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

                    if (!g.open_giveaway_url.isNullOrBlank()) {
                        Button(
                            onClick = {
                                uriHandler.openUri(g.open_giveaway_url!!)
                             },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("🎁 Get Game")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            BackButton(onClick = { navController.popBackStack() })
        }
    }
}

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
