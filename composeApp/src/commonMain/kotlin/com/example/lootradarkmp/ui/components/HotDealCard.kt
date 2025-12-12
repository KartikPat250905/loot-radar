package com.example.lootradarkmp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.lootradarkmp.data.models.GameDto

@Composable
fun HotDealCard(game: GameDto, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(end = 12.dp)
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = game.image,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(100.dp)
                    .width(160.dp)
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = game.title ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                GameWorth(game.worth)
            }
        }
    }
}
