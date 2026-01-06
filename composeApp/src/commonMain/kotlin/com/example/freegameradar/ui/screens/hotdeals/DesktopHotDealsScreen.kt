package com.example.freegameradar.ui.screens.hotdeals

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
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freegameradar.ui.components.HotDealCard
import com.example.freegameradar.ui.viewmodel.HotDealsViewModel
import kotlinx.coroutines.delay

@Composable
fun DesktopHotDealsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val vm = remember { HotDealsViewModel() }

    val featured by vm.featured.collectAsState()
    val highest by vm.highestValue.collectAsState()
    val expiring by vm.expiringSoon.collectAsState()
    val trending by vm.trending.collectAsState()

    var tabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Featured", "Highest Value", "Expiring Soon", "Trending")

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
            .padding(24.dp)
    ) {
        val heroItems = (featured.ifEmpty { highest }).take(5)

        if (heroItems.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { heroItems.size })
            val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

            LaunchedEffect(isDragged) {
                while (!isDragged) {
                    delay(3000)
                    val nextPage = (pagerState.currentPage + 1) % heroItems.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                pageSpacing = 24.dp,
                contentPadding = PaddingValues(horizontal = 100.dp)
            ) { page ->
                val game = heroItems[page]
                DesktopHeroBanner(game = game) {
                    game.id?.let { id -> navController.navigate("details/$id") }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0D1B2A),
                                    Color(0xFF1B263B),
                                    Color(0xFF0D1B2A)
                                )
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF1B263B),
                                shape = RoundedCornerShape(11.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${heroItems.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tabs.forEachIndexed { i, title ->
                DesktopTabButton(
                    title = title,
                    isSelected = tabIndex == i,
                    onClick = { tabIndex = i },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        val selectedList = when (tabIndex) {
            0 -> featured
            1 -> highest
            2 -> expiring
            3 -> trending
            else -> featured
        }

        if (selectedList.isEmpty()) {
            EmptyStateMessage(tabIndex)
        } else {
            key(tabIndex) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(selectedList) { game ->
                        HotDealCard(game = game) {
                            game.id?.let { id -> navController.navigate("details/$id") }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        if (highest.size > 5) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "More Hot Deals",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFE5E7EB),
                        letterSpacing = 0.5.sp
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(3.dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF34D399),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }

                Text(
                    "${highest.size - 5} deals",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF6B7280)
                )
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(highest.drop(5)) { game ->
                    HotDealCard(game = game) {
                        game.id?.let { id -> navController.navigate("details/$id") }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}
