package com.example.lootradarkmp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import com.example.lootradarkmp.data.models.GameDto
import com.example.lootradarkmp.data.repository.GameRepository
import com.example.lootradarkmp.ui.components.BackButton
import com.example.lootradarkmp.ui.components.GameWorth

@Composable
fun GameDetailScreen(
    navController: NavHostController,
    gameId: Int?,
    modifier: Modifier = Modifier
) {
    var game by remember { mutableStateOf<GameDto?>(null) }
    val repository = remember { GameRepository() }
    val uriHandler = LocalUriHandler.current

    // Load selected game based on ID
    LaunchedEffect(gameId) {
        repository.getFreeGames().collect { list ->
            game = list.find { it.id == gameId }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            game?.image?.let { imageUrl ->
                AsyncImage(
                    model = imageUrl,
                    contentDescription = game?.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            game?.let { g ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {

                    // Title
                    Text(
                        text = g.title ?: "Unknown Game",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Worth
                    GameWorth(price = g.worth)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Status
                    Text(
                        text = g.status ?: "Unknown",
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Platforms
                    Text(
                        text = "Platforms: ${g.platforms ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Description
                    Text(
                        text = g.description ?: "No description available",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Instructions
                    if (!g.instructions.isNullOrBlank()) {
                        Text(
                            text = "How to get it",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = g.instructions!!,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Giveaway Button
                    if (!g.open_giveaway_url.isNullOrBlank()) {
                        Button(
                            onClick = { uriHandler.openUri(g.open_giveaway_url!!) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text("🎁 Open Giveaway")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }

        // Styled Floating Back Button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            BackButton(onClick = { navController.popBackStack() })
        }
    }
}
