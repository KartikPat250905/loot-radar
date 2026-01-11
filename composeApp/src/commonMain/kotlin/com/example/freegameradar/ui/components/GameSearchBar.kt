package com.example.freegameradar.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.freegameradar.core.Platform

@Composable
fun GameSearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (Platform.isDesktop) {
        DesktopGameSearchBar(text, onTextChange, modifier)
    } else {
        MobileGameSearchBar(text, onTextChange, modifier)
    }
}

@Composable
private fun MobileGameSearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        onValueChange = onTextChange,
        label = { Text("Search games", color = Color(0xFF6EE7B7)) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF10B981)
            )
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { onTextChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color(0xFF9CA3AF)
                    )
                }
            }
        },
        placeholder = { Text("Type to search...", color = Color(0xFF6B7280)) },
        singleLine = true,
        shape = RoundedCornerShape(25.dp),
        modifier = modifier.fillMaxWidth(),
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

@Composable
private fun DesktopGameSearchBar(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = { Text("Search games...", color = Color(0xFF6B7280)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF10B981)
                )
            },
            trailingIcon = {
                if (text.isNotEmpty()) {
                    IconButton(onClick = { onTextChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
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
