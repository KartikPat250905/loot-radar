package com.example.freegameradar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.data.state.DataSource
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.components.GameItemCard
import com.example.freegameradar.ui.components.GameSearchBar
import com.example.freegameradar.ui.components.GameTypeFilterTabs
import com.example.freegameradar.ui.components.TotalWorthBar
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.GameTypeFilter
import com.example.freegameradar.ui.viewmodel.GameViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onBottomBarVisibilityChange: (Boolean) -> Unit
) {
    val gameViewModel = remember { GameViewModel() }

    var isLoading by remember { mutableStateOf(true) }
    val games by gameViewModel.games.collectAsState()
    val searchQuery by gameViewModel.searchQuery.collectAsState()
    val selectedFilter by gameViewModel.gameTypeFilter.collectAsState()
    val dataSource by gameViewModel.dataSource.collectAsState()

    val gridState = rememberLazyGridState()

    // Detect if running on Desktop
    val isDesktop = remember {
        System.getProperty("os.name")?.contains("Windows", ignoreCase = true) == true ||
                System.getProperty("os.name")?.contains("Mac", ignoreCase = true) == true ||
                System.getProperty("os.name")?.contains("Linux", ignoreCase = true) == true
    }

    var isVisible by remember { mutableStateOf(true) }

    // Only hide header on mobile, not desktop
    if (!isDesktop) {
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
    } else {
        // On desktop, always show header
        isVisible = true
    }

    LaunchedEffect(isVisible) {
        onBottomBarVisibilityChange(isVisible)
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
        // Animated on mobile, always visible on desktop
        if (isDesktop) {
            // Desktop: No animation, always visible
            Column {
                HeaderContent(
                    games = games,
                    searchQuery = searchQuery,
                    selectedFilter = selectedFilter,
                    dataSource = dataSource,
                    onSearchChange = { gameViewModel.updateSearch(it) },
                    onFilterChange = { gameViewModel.updateFilter(it) }
                )
            }
        } else {
            // Mobile: Animated visibility
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
                    HeaderContent(
                        games = games,
                        searchQuery = searchQuery,
                        selectedFilter = selectedFilter,
                        dataSource = dataSource,
                        onSearchChange = { gameViewModel.updateSearch(it) },
                        onFilterChange = { gameViewModel.updateFilter(it) }
                    )
                }
            }
        }

        // Content Area (same for both)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
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
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No games in database",
                            color = Color(0xFF9CA3AF),
                            fontSize = 18.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { gameViewModel.syncFromNetwork() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
                            Text(text = "Sync from Network")
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 280.dp),
                        state = gridState,
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = games,
                            key = { game -> game.id ?: game.hashCode() }
                        ) { game ->
                            GameItemCard(
                                gameDto = game,
                                onClick = {
                                    game.id?.let { id ->
                                        navController.navigate(
                                            Screen.Details.createRoute(id)
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Simplified header - no game count or reload button
@Composable
private fun HeaderContent(
    games: List<GameDto>,
    searchQuery: String,
    selectedFilter: GameTypeFilter,
    dataSource: DataSource,
    onSearchChange: (String) -> Unit,
    onFilterChange: (GameTypeFilter) -> Unit
) {
    // Search Bar
    GameSearchBar(
        text = searchQuery,
        onTextChange = onSearchChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )

    // Filter Tabs
    GameTypeFilterTabs(
        selectedFilter = selectedFilter,
        onFilterSelected = onFilterChange
    )

    // Total Worth Bar
    if (games.isNotEmpty()) {
        TotalWorthBar(
            games = games,
            dataSource = dataSource
        )
    }
}
