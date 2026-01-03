// HotDealsScreen.kt
package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freegameradar.ui.components.HeroBanner
import com.example.freegameradar.ui.components.HotDealCard
import com.example.freegameradar.ui.viewmodel.HotDealsViewModel
import kotlinx.coroutines.delay

@Composable
fun HotDealsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val vm = remember { HotDealsViewModel() }

    val featured by vm.featured.collectAsState()
    val highest by vm.highestValue.collectAsState()
    val expiring by vm.expiringSoon.collectAsState()
    val trending by vm.trending.collectAsState()

    var tabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Featured", "Highest Value", "Expiring", "Trending")

    LaunchedEffect(Unit) {
        vm.load()
    }

    DisposableEffect(Unit) {
        onDispose { vm.clear() }
    }

    Column(
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
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {

        // Pager for Hero Banner
        val heroItems = (featured.ifEmpty { highest }).take(5)

        if (heroItems.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { heroItems.size })
            val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

            LaunchedEffect(isDragged) {
                while (!isDragged) {
                    delay(2250)
                    val nextPage = (pagerState.currentPage + 1) % heroItems.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(240.dp),
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) { page ->
                val game = heroItems[page]
                HeroBanner(game = game) {
                    game.id?.let { id -> navController.navigate("details/$id") }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Styled page indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = Color(0xFF1B263B),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1} / ${heroItems.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Styled tabs
        TabRow(
            selectedTabIndex = tabIndex,
            containerColor = Color(0xFF0D1B2A),
            indicator = { tabPositions ->
                if (tabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                        height = 3.dp,
                        color = Color(0xFF10B981)
                    )
                }
            },
            divider = {},
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = tabIndex == i,
                    onClick = { tabIndex = i },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (tabIndex == i) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (tabIndex == i) Color(0xFF10B981) else Color(0xFF9CA3AF)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Carousel for selected tab
        val selectedList = when (tabIndex) {
            0 -> featured
            1 -> highest
            2 -> expiring
            3 -> trending
            else -> featured
        }

        if (selectedList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                if (tabIndex == 2) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "No games expiring soon (2 days)!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9CA3AF),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "All current deals are available for a while!",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text(
                        "No deals found",
                        fontSize = 16.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
        } else {
            key(tabIndex) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(0.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(selectedList) { game ->
                        HotDealCard(game = game) {
                            game.id?.let { id -> navController.navigate("details/$id") }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // "More Hot Deals" section
        Text(
            "More Hot Deals",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE5E7EB),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(highest.drop(5)) { game ->
                HotDealCard(game = game) {
                    game.id?.let { id -> navController.navigate("details/$id") }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
