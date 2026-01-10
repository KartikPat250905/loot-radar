package com.example.freegameradar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GameWorth(price: String?) {
    Row {
        Text(
            text = price ?: "N/A",
            style = MaterialTheme.typography.bodyMedium.copy(
                textDecoration = TextDecoration.LineThrough,
                color = Color(0xFF6B7280),
                fontSize = 13.sp
            )
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "FREE",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color(0xFF10B981),
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
        )
    }
}