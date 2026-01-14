package com.example.freegameradar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
    val remainingCooldown by remember {
        derivedStateOf { gameViewModel.getRemainingCooldown() }
    }

    val searchText by gameViewModel.searchQuery.collectAsState()
    val dataSource by gameViewModel.dataSource.collectAsState()
    val selectedFilter by gameViewModel.gameTypeFilter.collectAsState()
    val gridState = rememberLazyGridState()
    var isVisible by remember { mutableStateOf(true) }

    // Existing scroll detection logic
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

        // Existing game grid
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            if (isLoading) {
                AppLoadingScreen(fullScreen = false)
            } else if (games.isEmpty() && dataSource == DataSource.CACHE) {
                Text(
                    text = "😿 No freebies found!\nCache is empty and new data couldn't load.\nCheck your internet connection and try again.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    color = Color(0xFF9CA3AF)
                )
            } else if (games.isEmpty()) {
                Text(
                    text = "No freebies found here... 😥\nTry adjusting your filters!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    color = Color(0xFF9CA3AF)
                )
            } else {
                GameGrid(
                    games,
                    navController,
                    gridState = gridState
                )
            }
        }
    }

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