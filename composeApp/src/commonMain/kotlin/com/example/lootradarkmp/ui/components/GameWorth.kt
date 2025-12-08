package com.example.lootradarkmp.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun GameWorth(price: String?) {
    Row {
        // Original price crossed out
        Text(
            text = price ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.LineThrough,
                color = Color.Gray
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        // "$0.00" in green
        Text(
            text = "$0.00",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF4CAF50) // nice green
            )
        )
    }
}
