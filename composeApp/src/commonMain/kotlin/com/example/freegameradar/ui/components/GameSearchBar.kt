package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun GameSearchBar(
    text: String,
    onTextChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            label = { Text("Search games", color = Color(0xFF6EE7B7)) },
            singleLine = true,
            shape = RoundedCornerShape(25.dp),
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF374151),
                focusedTextColor = Color(0xFFE5E7EB),
                unfocusedTextColor = Color(0xFFE5E7EB),
                cursorColor = Color(0xFF10B981),
                focusedContainerColor = Color(0xFF1B263B),
                unfocusedContainerColor = Color(0xFF0D1B2A)
            )
        )
    }
}