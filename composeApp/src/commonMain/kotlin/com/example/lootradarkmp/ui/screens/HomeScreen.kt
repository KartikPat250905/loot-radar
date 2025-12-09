package com.example.lootradarkmp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.lootradarkmp.data.repository.GameRepository
import com.example.lootradarkmp.ui.components.FilterBar
import com.example.lootradarkmp.ui.components.GameGrid
import com.example.lootradarkmp.ui.components.GameSearchBar
import com.example.lootradarkmp.ui.components.TotalWorthBar
import com.example.lootradarkmp.ui.viewmodel.GameViewModel

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val gameViewModel = remember { GameViewModel() }
    var isLoading by remember { mutableStateOf(true) }
    val games by gameViewModel.games.collectAsState()
    var searchText by remember { mutableStateOf("") }
    Column (
        modifier = modifier.fillMaxSize()
    ) {
        GameSearchBar(
            searchText,
            onTextChange = {
                searchText = it
                gameViewModel.updateSearch(it)
            }
        )
        FilterBar(gameViewModel)
        TotalWorthBar(games = games)
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator()
            }
            else if (games.isEmpty()) {
                Text(
                    text = "No freebies found here... \uD83D\uDE22\nTry adjusting your filters!",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp
                )
            }
            else {
                GameGrid(games)
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
