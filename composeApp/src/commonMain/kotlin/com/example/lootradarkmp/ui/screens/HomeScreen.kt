package com.example.lootradarkmp.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.example.lootradarkmp.data.repository.GameRepository
import com.example.lootradarkmp.ui.components.GameGrid

@Composable
fun HomeScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    // 1. Create the repository. 'remember' keeps this object alive as long as this screen is on.
    // It prevents us from creating a new repository every time the screen redraws.
    val repository = remember { GameRepository() }

    // 2. Listen to the data flow.
    // 'collectAsState' converts the data stream (Flow) into a State that Compose can read.
    // 'initial = null' means we start with no data (loading state) before the first data arrives.
    val gamesState by repository.getFreeGames().collectAsState(initial = null)

    Box(modifier = modifier.fillMaxSize()) {
        val games = gamesState
        
        // 3. Check if we have data yet.
        if (games == null) {
            // If games is null, we are still waiting for the network, so show a loading spinner.
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            // If games is not null, we have the list! Show the grid of games.
            GameGrid(games)
        }
    }
}
