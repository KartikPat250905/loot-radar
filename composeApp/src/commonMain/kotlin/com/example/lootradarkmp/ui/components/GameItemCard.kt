package com.example.lootradarkmp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import com.example.lootradarkmp.data.models.GameDto

@Composable
fun GameItemCard(gameDto: GameDto) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            val imageUrl = gameDto.image.orEmpty()
            
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = gameDto.title.orEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop,
                loading = {
                    CircularProgressIndicator()
                },
                error = {
                    Text("No image")
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = gameDto.title ?: "No title found")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = gameDto.type ?: "Unknown Type")
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = gameDto.worth ?: "Free")
        }
    }
}
