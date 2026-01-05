package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.ui.components.RemoteImage
import com.example.freegameradar.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val games by homeViewModel.games.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
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
    // Use thumbnail first, fallback to image
    val imageUrl = game.thumbnail ?: game.image
    
    println("GameCard: ${game.title} - Thumbnail: ${game.thumbnail}, Image: ${game.image}")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(100.dp, 75.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                if (!imageUrl.isNullOrEmpty()) {
                    RemoteImage(
                        url = imageUrl,
                        contentDescription = game.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎮", style = MaterialTheme.typography.headlineSmall)
                    }
                }
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
                                    text = type,
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
