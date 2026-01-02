package com.example.freegameradar.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.freegameradar.data.models.GameDto
import com.example.freegameradar.ui.navigation.Screen

@Composable
fun GameGrid(
    gameList: List<GameDto>,
    navController: NavHostController,
    gridState: LazyGridState = rememberLazyGridState(),
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2), // 2 items per row
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(gameList) { game ->
            GameItemCard(gameDto = game) {
                navController.navigate(
                    Screen.Details.createRoute(game.id)
                )
            }
        }
    }
}
