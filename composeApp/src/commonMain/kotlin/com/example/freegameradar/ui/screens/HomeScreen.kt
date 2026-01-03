package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val games by homeViewModel.games.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎮 Free Games",
                    style = MaterialTheme.typography.headlineMedium
                )

                IconButton(onClick = { homeViewModel.refresh() }) {
                    Text(text = "🔄", style = MaterialTheme.typography.titleLarge)
                }
            }
        }

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Loading games...")
                    }
                }
            }

            error != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(text = "⚠️ Error: $error")
                        Button(onClick = { homeViewModel.refresh() }) {
                            Text(text = "Retry")
                        }
                    }
                }
            }

            games.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "No games in database")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Games need to be synced first", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            text = "${games.size} games available",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    items(
                        items = games,
                        key = { game -> game.id ?: game.hashCode() }
                    ) { game ->
                        GameCard(game = game)
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(game: GameDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            // Placeholder instead of image for now
            Box(
                modifier = Modifier
                    .size(100.dp, 75.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text("🎮", style = MaterialTheme.typography.headlineSmall)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                game.title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = game.description ?: "No description available",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    game.platforms?.let { platforms ->
                        AssistChip(
                            onClick = {},
                            label = { 
                                Text(
                                    text = platforms,
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            }
                        )
                    }
                    game.type?.let { type ->
                        AssistChip(
                            onClick = {},
                            label = { 
                                Text(
                                    text = type.toString(),
                                    style = MaterialTheme.typography.labelSmall
                                ) 
                            }
                        )
                    }
                }
            }
        }
    }
}
