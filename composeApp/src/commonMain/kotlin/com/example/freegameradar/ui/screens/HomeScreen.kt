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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.ui.components.GameItemCard
import com.example.freegameradar.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(homeViewModel: HomeViewModel) {
    val games by homeViewModel.games.collectAsState()
    val isLoading by homeViewModel.isLoading.collectAsState()
    val error by homeViewModel.error.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D1B2A)) // Dark background
    ) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF1B263B)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "🎮 Free Games",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE5E7EB)
                    )
                    Text(
                        text = "${games.size} games available",
                        fontSize = 13.sp,
                        color = Color(0xFF9CA3AF)
                    )
                }

                IconButton(onClick = { homeViewModel.refresh() }) {
                    Text(
                        text = "🔄",
                        fontSize = 24.sp
                    )
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
                        CircularProgressIndicator(color = Color(0xFF10B981))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading games...",
                            color = Color(0xFF9CA3AF)
                        )
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
                        Text(
                            text = "⚠️ Error: $error",
                            color = Color(0xFFEF4444)
                        )
                        Button(
                            onClick = { homeViewModel.refresh() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981)
                            )
                        ) {
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
                        Text(
                            text = "No games in database",
                            color = Color(0xFF9CA3AF)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Games need to be synced first",
                            fontSize = 13.sp,
                            color = Color(0xFF6B7280)
                        )
                    }
                }
            }

            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 280.dp),
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
                                println("Clicked: ${game.title}")
                                // TODO: Navigate to game detail
                            }
                        )
                    }
                }
            }
        }
    }
}