package com.radarlabs.freegameradar.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.radarlabs.freegameradar.data.state.DataSource
import com.radarlabs.freegameradar.ui.components.AppLoadingScreen
import com.radarlabs.freegameradar.ui.components.GameGrid
import com.radarlabs.freegameradar.ui.components.GameTypeFilterTabs
import com.radarlabs.freegameradar.ui.components.SearchAndRefreshBar
import com.radarlabs.freegameradar.ui.components.TotalWorthBar
import com.radarlabs.freegameradar.ui.navigation.Screen
import com.radarlabs.freegameradar.ui.viewmodel.GameTypeFilter
import com.radarlabs.freegameradar.ui.viewmodel.GameViewModel
import com.radarlabs.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.radarlabs.freegameradar.util.isNotificationPermissionGranted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.roundToInt

private object PermissionPromptTracker {
    var hasShownThisSession = false
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    gameViewModel: GameViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
    modifier: Modifier = Modifier,
    onBottomBarVisibilityChange: (Boolean) -> Unit,
    onShowRefreshAd: () -> Unit = {}
) {
    val games by gameViewModel.games.collectAsState()
    val isRefreshing by gameViewModel.isRefreshing.collectAsState()
    val canRefresh by gameViewModel.canRefresh.collectAsState()
    val remainingCooldown by gameViewModel.remainingCooldown.collectAsState()
    val preferencesState by userPreferencesViewModel.uiState.collectAsState()
    val searchText by gameViewModel.searchQuery.collectAsState()
    val dataSource by gameViewModel.dataSource.collectAsState()
    val selectedFilter by gameViewModel.gameTypeFilter.collectAsState()
    val totalWorth by gameViewModel.totalWorth.collectAsState()
    val gridState = rememberLazyGridState()

    val apiWorthForBar = if (selectedFilter == GameTypeFilter.ALL && searchText.isBlank()) {
        totalWorth?.worthEstimationUsd
    } else null

    val density = LocalDensity.current

    // ✅ Measured at runtime — no hardcoded height
    var topBarHeightPx by remember { mutableIntStateOf(0) }

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(
        state = rememberTopAppBarState()
    )

    // ✅ Update limit whenever real height is measured
    LaunchedEffect(topBarHeightPx) {
        if (topBarHeightPx > 0) {
            scrollBehavior.state.heightOffsetLimit = -topBarHeightPx.toFloat()
        }
    }

    // ✅ Offset in pixels directly — used for both bar position AND grid padding
    val topBarOffsetPx = scrollBehavior.state.heightOffset.roundToInt()
    val topBarOffsetDp = with(density) { topBarOffsetPx.toDp() }

    // ✅ Grid top padding = real height + animated offset (shrinks to 0 as bar hides)
    val gridTopPaddingPx = (topBarHeightPx + topBarOffsetPx).coerceAtLeast(0)
    val gridTopPaddingDp = with(density) { gridTopPaddingPx.toDp() }

    val isTopBarVisible = topBarOffsetPx > -(topBarHeightPx / 2)
    LaunchedEffect(isTopBarVisible) {
        onBottomBarVisibilityChange(isTopBarVisible)
    }

    val permissionState = isNotificationPermissionGranted()
    var showPermissionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(preferencesState.isLoaded, preferencesState.notificationsEnabled, permissionState) {
        if (preferencesState.isLoaded &&
            preferencesState.notificationsEnabled &&
            !permissionState &&
            !PermissionPromptTracker.hasShownThisSession
        ) {
            delay(500)
            PermissionPromptTracker.hasShownThisSession = true
            showPermissionDialog = true
        }
    }

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = "Notifications",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(48.dp)
                )
            },
            title = {
                Text(
                    text = "Welcome Back!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "It looks like you're on a new device or notification permissions have changed.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "To continue receiving alerts about free games, please update your notification permissions for this device.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color(0xFFE5E7EB)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPermissionDialog = false
                        navController.navigate(Screen.Settings.route)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF60A5FA)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Go to Settings",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text(text = "Maybe Later", color = Color(0xFF9CA3AF))
                }
            },
            containerColor = Color(0xFF1B263B),
            shape = RoundedCornerShape(16.dp)
        )
    }

    LaunchedEffect(Unit) {
        gameViewModel.showRefreshAd.collectLatest { onShowRefreshAd() }
    }

    Box(
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
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // ✅ Grid top padding tracks the bar in real time — no dead space ever
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = gridTopPaddingDp),
            contentAlignment = Alignment.Center
        ) {
            when {
                games.isEmpty() && dataSource == DataSource.CACHE -> {
                    Log.d("HomeScreen", "📺 RENDERING: Cache empty message")
                    Text(
                        text = "😿 No freebies found!\nCache is empty and new data couldn't load.\nCheck your internet connection and try again.",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(16.dp),
                        fontSize = 18.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }
                games.isEmpty() -> {
                    Log.d("HomeScreen", "📺 RENDERING: Loading with AppLoadingScreen")
                    AppLoadingScreen(fullScreen = false)
                }
                else -> {
                    Log.d("HomeScreen", "📺 RENDERING: Game grid with ${games.size} games")
                    GameGrid(
                        gameList = games,
                        navController = navController,
                        gridState = gridState,
                        bottomContentPadding = 88.dp
                    )
                }
            }

            if (isRefreshing) {
                Log.d("HomeScreen", "📺 RENDERING: Refresh overlay")
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
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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

        // ✅ Top bar measured at runtime, offset pixel-perfectly with scroll
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    // ✅ Capture real height once laid out
                    if (coords.size.height != topBarHeightPx) {
                        topBarHeightPx = coords.size.height
                    }
                }
                .offset { IntOffset(x = 0, y = topBarOffsetPx) }
                .background(Color(0xFF0D1B2A))
        ) {
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
                dataSource = dataSource,
                apiWorth = apiWorthForBar
            )
        }
    }

    LaunchedEffect(selectedFilter, searchText) {
        gridState.scrollToItem(0)
        scrollBehavior.state.heightOffset = 0f
    }

    LaunchedEffect(Unit) {
        Log.d("HomeScreen", "🚀 Calling loadGames()")
        gameViewModel.loadGames()
    }

    DisposableEffect(Unit) {
        onDispose { gameViewModel.clear() }
    }
}
