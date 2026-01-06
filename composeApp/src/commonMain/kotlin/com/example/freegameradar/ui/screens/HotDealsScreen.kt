package com.example.freegameradar.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.ui.components.HeroBanner
import com.example.freegameradar.ui.components.HotDealCard
import com.example.freegameradar.ui.viewmodel.HotDealsViewModel
import kotlinx.coroutines.delay

@Composable
fun HotDealsScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val isDesktop = remember {
        System.getProperty("os.name")?.let { os ->
            os.contains("Windows", ignoreCase = true) ||
                    os.contains("Mac", ignoreCase = true) ||
                    os.contains("Linux", ignoreCase = true)
        } ?: false
    }

    if (isDesktop) {
        DesktopHotDealsScreen(navController, modifier)
    } else {
        MobileHotDealsScreen(navController, modifier)
    }
}

// Mobile Version - Original Design
@Composable
private fun MobileHotDealsScreen(
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

// Enhanced Desktop Version
@Composable
private fun DesktopHotDealsScreen(
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

            // REMOVED THE SEPARATE GLOW BOX - now integrated into the pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),  // Changed from 450dp to 400dp to match card height
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

@Composable
private fun DesktopHeroBanner(game: GameDto?, onClick: () -> Unit) {
    if (game == null) return

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B263B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = game.image,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0x20000000),
                                Color(0x80000000),
                                Color(0xCC0D1B2A)
                            )
                        )
                    )
            )

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

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(32.dp)
            ) {
                Text(
                    text = game.title ?: "",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE5E7EB),
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = game.worth ?: "Free",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981)
                )
            }
        }
    }
}

@Composable
private fun DesktopTabButton(
    title: String,
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
        modifier = modifier.height(56.dp)
    ) {
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
                            radius = 400f
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            )
        }

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
                                Color(0xFF374151).copy(alpha = 0.3f),
                                Color(0xFF1F2937).copy(alpha = 0.3f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(if (isSelected) 2.dp else 1.dp)
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
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(2.dp)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
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
                    text = title,
                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = if (isSelected) Color(0xFF6EE7B7) else Color(0xFF6B7280),
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyStateMessage(tabIndex: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1B263B).copy(alpha = 0.5f),
                        Color.Transparent
                    ),
                    radius = 600f
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (tabIndex == 2) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🎉",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "No games expiring soon!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE5E7EB),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "All current deals are available for a while!",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "🔍",
                    fontSize = 48.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No deals found",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE5E7EB)
                )
            }
        }
    }
}
