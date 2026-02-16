package com.radarlabs.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.*

@Composable
fun ClaimStatsCards(
    claimedGamesWithTimestamps: Map<Long, Long>,
    modifier: Modifier = Modifier
) {
    val stats = remember(claimedGamesWithTimestamps) {
        calculateStats(claimedGamesWithTimestamps)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Streak Card
        StreakCard(
            currentStreak = stats.currentStreak,
            longestStreak = stats.longestStreak
        )

        // Activity Overview Card
        ActivityOverviewCard(
            totalClaims = stats.totalClaims,
            thisWeek = stats.claimsThisWeek,
            thisMonth = stats.claimsThisMonth,
            last7Days = stats.last7DaysClaims
        )

        // Milestones Card
        MilestonesCard(
            totalClaims = stats.totalClaims,
            daysActive = stats.daysWithClaims
        )
    }
}

@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.15f),
                        Color(0xFF34D399).copy(alpha = 0.1f),
                        Color(0xFF10B981).copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B263B),
                            Color(0xFF0D1B2A)
                        )
                    ),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Current Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔥",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "$currentStreak",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF10B981),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Day Streak",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                }

                // Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(80.dp)
                        .align(Alignment.CenterVertically)
                        .background(Color(0xFF374151))
                )

                // Longest Streak
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "⭐",
                        fontSize = 32.sp
                    )
                    Text(
                        text = "$longestStreak",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF6EE7B7),
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Best Streak",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF9CA3AF),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ActivityOverviewCard(
    totalClaims: Int,
    thisWeek: Int,
    thisMonth: Int,
    last7Days: List<Int>
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.15f),
                        Color(0xFF34D399).copy(alpha = 0.1f),
                        Color(0xFF10B981).copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B263B),
                            Color(0xFF0D1B2A)
                        )
                    ),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "📊 Activity Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE5E7EB),
                    letterSpacing = 0.5.sp
                )

                // Stats Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SmallStatItem("Total", "$totalClaims")
                    SmallStatItem("This Week", "$thisWeek")
                    SmallStatItem("This Month", "$thisMonth")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Mini bar chart for last 7 days
                Text(
                    text = "Last 7 Days",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6EE7B7),
                    letterSpacing = 0.3.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    val maxClaims = last7Days.maxOrNull() ?: 1
                    last7Days.forEach { claims ->
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (claims > 0) {
                                Text(
                                    text = "$claims",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .width(24.dp)
                                    .height(((claims.toFloat() / maxClaims.toFloat()) * 60).coerceAtLeast(4f).dp)
                                    .background(
                                        color = if (claims > 0) Color(0xFF10B981) else Color(0xFF374151),
                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MilestonesCard(
    totalClaims: Int,
    daysActive: Int
) {
    val milestones = listOf(
        Milestone("First Claim", 1, "🎯"),
        Milestone("Getting Started", 5, "🌱"),
        Milestone("Collector", 10, "📦"),
        Milestone("Enthusiast", 25, "⭐"),
        Milestone("Expert", 50, "💎"),
        Milestone("Legend", 100, "👑")
    )

    val nextMilestone = milestones.firstOrNull { it.target > totalClaims }
    val progress = if (nextMilestone != null) {
        totalClaims.toFloat() / nextMilestone.target.toFloat()
    } else {
        1f
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF10B981).copy(alpha = 0.15f),
                        Color(0xFF34D399).copy(alpha = 0.1f),
                        Color(0xFF10B981).copy(alpha = 0.15f)
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B263B),
                            Color(0xFF0D1B2A)
                        )
                    ),
                    shape = RoundedCornerShape(15.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🏆 Your Progress",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE5E7EB),
                    letterSpacing = 0.5.sp
                )

                if (nextMilestone != null) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${nextMilestone.emoji} ${nextMilestone.name}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE5E7EB)
                            )
                            Text(
                                text = "$totalClaims / ${nextMilestone.target}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF10B981),
                            trackColor = Color(0xFF374151)
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "👑",
                            fontSize = 48.sp
                        )
                        Text(
                            text = "All Milestones Complete!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Achievement badges with names
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    milestones.take(6).forEach { milestone ->
                        val achieved = totalClaims >= milestone.target
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = milestone.emoji,
                                fontSize = 20.sp,
                                color = if (achieved) Color.White else Color(0xFF6B7280)
                            )
                            Text(
                                text = "${milestone.target}",
                                fontSize = 9.sp,
                                fontWeight = if (achieved) FontWeight.Bold else FontWeight.Normal,
                                color = if (achieved) Color(0xFF10B981) else Color(0xFF6B7280)
                            )
                            Text(
                                text = milestone.name.split(" ").first(),
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (achieved) Color(0xFF9CA3AF) else Color(0xFF4B5563),
                                textAlign = TextAlign.Center,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallStatItem(label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF10B981),
            letterSpacing = 0.5.sp
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF9CA3AF),
            textAlign = TextAlign.Center
        )
    }
}

private data class Milestone(
    val name: String,
    val target: Int,
    val emoji: String
)

private data class ClaimStats(
    val totalClaims: Int,
    val currentStreak: Int,
    val longestStreak: Int,
    val claimsThisWeek: Int,
    val claimsThisMonth: Int,
    val daysWithClaims: Int,
    val last7DaysClaims: List<Int>
)

private fun calculateStats(claimedGamesWithTimestamps: Map<Long, Long>): ClaimStats {
    if (claimedGamesWithTimestamps.isEmpty()) {
        return ClaimStats(0, 0, 0, 0, 0, 0, List(7) { 0 })
    }

    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

    val claimsByDate = claimedGamesWithTimestamps.values
        .groupBy { timestamp ->
            val instant = Instant.fromEpochMilliseconds(timestamp)
            instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
        }
        .mapValues { it.value.size }

    // Calculate streaks
    val sortedDates = claimsByDate.keys.sorted()
    var currentStreak = 0
    var longestStreak = 0
    var tempStreak = 1

    for (i in sortedDates.indices) {
        if (i > 0) {
            val dayDiff = sortedDates[i - 1].daysUntil(sortedDates[i])
            if (dayDiff == 1) {
                tempStreak++
            } else {
                longestStreak = maxOf(longestStreak, tempStreak)
                tempStreak = 1
            }
        }
    }
    longestStreak = maxOf(longestStreak, tempStreak)

    // Current streak
    if (sortedDates.isNotEmpty()) {
        val lastClaimDate = sortedDates.last()
        val daysSinceLastClaim = lastClaimDate.daysUntil(today)

        if (daysSinceLastClaim <= 1) {
            currentStreak = 1
            for (i in sortedDates.size - 2 downTo 0) {
                val dayDiff = sortedDates[i].daysUntil(sortedDates[i + 1])
                if (dayDiff == 1) {
                    currentStreak++
                } else {
                    break
                }
            }
        }
    }

    // This week and month
    val weekStart = today.minus(today.dayOfWeek.ordinal, DateTimeUnit.DAY)
    val monthStart = LocalDate(today.year, today.month, 1)

    val claimsThisWeek = claimedGamesWithTimestamps.values.count { timestamp ->
        val date = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        date >= weekStart
    }

    val claimsThisMonth = claimedGamesWithTimestamps.values.count { timestamp ->
        val date = Instant.fromEpochMilliseconds(timestamp)
            .toLocalDateTime(TimeZone.currentSystemDefault()).date
        date >= monthStart
    }

    // Last 7 days
    val last7Days = (6 downTo 0).map { daysAgo ->
        val date = today.minus(daysAgo, DateTimeUnit.DAY)
        claimsByDate[date] ?: 0
    }

    return ClaimStats(
        totalClaims = claimedGamesWithTimestamps.size,
        currentStreak = currentStreak,
        longestStreak = longestStreak,
        claimsThisWeek = claimsThisWeek,
        claimsThisMonth = claimsThisMonth,
        daysWithClaims = claimsByDate.size,
        last7DaysClaims = last7Days
    )
}