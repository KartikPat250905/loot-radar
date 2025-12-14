package com.example.lootradarkmp.ui.screens

import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.lootradarkmp.ui.components.HeroBanner
import com.example.lootradarkmp.ui.components.HotDealCard
import com.example.lootradarkmp.ui.viewmodel.HotDealsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

    // Main container with vertical scroll to ensure everything fits even on smaller screens
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        
        // Pager for Hero Banner (Top 5 items)
        val heroItems = (featured.ifEmpty { highest }).take(5)
        
        if (heroItems.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { heroItems.size })
            val isDragged by pagerState.interactionSource.collectIsDraggedAsState()

            LaunchedEffect(isDragged) {
                while (!isDragged) {
                    delay(2250) // Auto-advance every 5 seconds
                    val nextPage = (pagerState.currentPage + 1) % heroItems.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth().height(220.dp), // Fixed height for hero area
                pageSpacing = 16.dp,
                contentPadding = PaddingValues(horizontal = 0.dp) // Show full card
            ) { page ->
                val game = heroItems[page]
                HeroBanner(game = game) {
                    game.id?.let { id -> navController.navigate("details/$id") }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Simple indicator
            Text(
                text = "${pagerState.currentPage + 1} / ${heroItems.size}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tabs
        ScrollableTabRow(
            selectedTabIndex = tabIndex,
            edgePadding = 0.dp,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { i, title ->
                Tab(
                    selected = tabIndex == i, 
                    onClick = { tabIndex = i }, 
                    text = { Text(title) }
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
                // Customized empty state message
                if (tabIndex == 2) { // Expiring tab
                     Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No games expiring soon (2 days)!",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "All current deals are available for a while!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                             textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Text("No deals found", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            // Using key(tabIndex) ensures the LazyRow is recreated when tab changes,
            // resetting the scroll position to the beginning.
            key(tabIndex) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    items(selectedList) { game ->
                        HotDealCard(game = game) {
                            game.id?.let { id -> navController.navigate("details/$id") }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // "More Hot Deals" section at the bottom
        Text("More Hot Deals", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(12.dp))

        // Another horizontal list for "More" (skipping the first few to avoid duplicates if possible)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
        ) {
            items(highest.drop(5)) { game ->
                HotDealCard(game = game) {
                    game.id?.let { id -> navController.navigate("details/$id") }
                }
            }
        }
        
        // Add some bottom padding so the last item isn't cut off by navigation bars etc
        Spacer(modifier = Modifier.height(80.dp))
    }
}
