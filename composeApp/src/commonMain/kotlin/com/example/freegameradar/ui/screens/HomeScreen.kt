package com.example.freegameradar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.freegameradar.data.state.DataSource
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.components.GameGrid
import com.example.freegameradar.ui.components.GameTypeFilterTabs
import com.example.freegameradar.ui.components.SearchAndRefreshBar
import com.example.freegameradar.ui.components.TotalWorthBar
import com.example.freegameradar.ui.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onBottomBarVisibilityChange: (Boolean) -> Unit
) {
    val gameViewModel: GameViewModel = viewModel()
    var isLoading by remember { mutableStateOf(true) }
    val games by gameViewModel.games.collectAsState()
    val isRefreshing by gameViewModel.isRefreshing.collectAsState()
    val canRefresh by gameViewModel.canRefresh.collectAsState()
    val remainingCooldown by gameViewModel.remainingCooldown.collectAsState()

    val searchText by gameViewModel.searchQuery.collectAsState()
    val dataSource by gameViewModel.dataSource.collectAsState()
    val selectedFilter by gameViewModel.gameTypeFilter.collectAsState()
    val gridState = rememberLazyGridState()
    var isVisible by remember { mutableStateOf(true) }

    // Scroll detection for hiding/showing top bar
    LaunchedEffect(gridState) {
        var previousIndex = gridState.firstVisibleItemIndex
        var previousOffset = gridState.firstVisibleItemScrollOffset

        snapshotFlow {
            gridState.firstVisibleItemIndex to gridState.firstVisibleItemScrollOffset
        }.collect { (index, offset) ->
            if (index == 0 && offset < 20) {
                isVisible = true
            } else {
                val isScrollingDown = if (index != previousIndex) {
                    index > previousIndex
                } else {
                    offset > previousOffset
                }

                if (isScrollingDown && index > 0) {
                    isVisible = false
                } else if (!isScrollingDown) {
                    isVisible = true
                }
            }

            previousIndex = index
            previousOffset = offset
        }
    }

    LaunchedEffect(isVisible) {
        onBottomBarVisibilityChange(isVisible)
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
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top bar with search, filters, and refresh
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                initialOffsetY = { -it }
            ) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                expandFrom = Alignment.Top
            ),
            exit = slideOutVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                targetOffsetY = { -it }
            ) + shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                shrinkTowards = Alignment.Top
            )
        ) {
            Column {
                SearchAndRefreshBar(
                    searchText = searchText,
                    onSearchChange = { gameViewModel.updateSearch(it) },
                    isRefreshing = isRefreshing,
                    canRefresh = canRefresh,
                    remainingSeconds = remainingCooldown,
                    onRefreshClick = { gameViewModel.refreshGames() }
                )

                GameTypeFilterTabs(
                    selectedFilter = selectedFilter,
                    onFilterSelected = { gameViewModel.updateFilter(it) }
                )

                TotalWorthBar(
                    games = games,
                    dataSource = dataSource
                )
            }
        }

        // Main content area with loading overlay
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f)
        ) {
            // Base content (games grid or empty states)
            when {
                isLoading -> {
                    AppLoadingScreen(fullScreen = false)
                }
                games.isEmpty() && dataSource == DataSource.CACHE -> {
                    Text(
                        text = "😿 No freebies found!\nCache is empty and new data couldn't load.\nCheck your internet connection and try again.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                games.isEmpty() -> {
                    Text(
                        text = "No freebies found here... 😥\nTry adjusting your filters!",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                else -> {
                    GameGrid(
                        games,
                        navController,
                        gridState = gridState
                    )
                }
            }

            // ✅ FIXED: Refresh loading overlay (conditional rendering instead of AnimatedVisibility)
            if (isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(32.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1B263B)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(56.dp),
                                color = Color(0xFF60A5FA),
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Refreshing games...",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Fetching latest deals",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        gameViewModel.loadGames()
        isLoading = false
    }

    DisposableEffect(Unit) {
        onDispose {
            gameViewModel.clear()
        }
    }
}
