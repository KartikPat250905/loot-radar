package com.radarlabs.freegameradar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun GameWorth(price: String?) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        // Prevent the Row itself from expanding and wrapping
        modifier = Modifier.wrapContentWidth()
    ) {
        Text(
            text = price ?: "Unknown",
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.LineThrough,
                color = Color.Gray
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(4.dp)) // reduced from 8dp

        Text(
            text = "\$0.00",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF4CAF50)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}