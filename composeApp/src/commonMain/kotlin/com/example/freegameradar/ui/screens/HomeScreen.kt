package com.example.freegameradar.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freegameradar.data.state.DataSource
import com.example.freegameradar.ui.components.FilterBar
import com.example.freegameradar.ui.components.GameGrid
import com.example.freegameradar.ui.components.GameSearchBar
import com.example.freegameradar.ui.components.GameTypeFilterTabs
import com.example.freegameradar.ui.components.TotalWorthBar
import com.example.freegameradar.ui.viewmodel.GameViewModel

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val gameViewModel = remember { GameViewModel() }
    var isLoading by remember { mutableStateOf(true) }
    val games by gameViewModel.games.collectAsState()
    var searchText by remember { mutableStateOf("") }
    val dataSource by gameViewModel.dataSource.collectAsState()
    val selectedFilter by gameViewModel.gameTypeFilter.collectAsState()
    val gridState = rememberLazyGridState()
    var isVisible by remember { mutableStateOf(true) }

    LaunchedEffect(gridState) {
        var previousOffset = gridState.firstVisibleItemScrollOffset
        snapshotFlow { gridState.firstVisibleItemScrollOffset }
            .collect { currentOffset ->
                if (currentOffset > previousOffset) {
                    isVisible = false
                } else if (currentOffset < previousOffset) {
                    isVisible = true
                }
                previousOffset = currentOffset
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .animateContentSize()
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(expandFrom = Alignment.Top),
            exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically(shrinkTowards = Alignment.Top)
        ) {
            Column {
                GameSearchBar(
                    searchText,
                    onTextChange = {
                        searchText = it
                        gameViewModel.updateSearch(it)
                    }
                )
                FilterBar(gameViewModel)
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
        Box(contentAlignment = Alignment.Center, modifier = Modifier.weight(1f)) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (games.isEmpty() && dataSource == DataSource.CACHE) {
                Text(
                    text = "\uD83D\uDE3F No freebies found!\nCache is empty and new data couldn't load.\nCheck your internet connection and try again.",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp
                )
            } else if (games.isEmpty()) {
                Text(
                    text = "No freebies found here... \uD83D\uDE25\nTry adjusting your filters!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp
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
