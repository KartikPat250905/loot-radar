package com.radarlabs.freegameradar.ui.components

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
import com.radarlabs.freegameradar.data.models.GameDto
import com.radarlabs.freegameradar.ui.navigation.Screen

@Composable
fun GameGrid(
    gameList: List<GameDto>,
    navController: NavHostController,
    gridState: LazyGridState = rememberLazyGridState(),
    bottomContentPadding: androidx.compose.ui.unit.Dp = 80.dp,
) {
    LazyVerticalGrid(
        state = gridState,
        columns = GridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 8.dp,
            end = 8.dp,
            top = 8.dp,
            bottom = bottomContentPadding
        )
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
