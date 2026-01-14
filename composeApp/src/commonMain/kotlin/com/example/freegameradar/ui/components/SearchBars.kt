package com.example.freegameradar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SearchAndRefreshBar(
    searchText: String,
    onSearchChange: (String) -> Unit,
    isRefreshing: Boolean,
    canRefresh: Boolean,
    remainingSeconds: Int,
    onRefreshClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search bar - takes most of the space
        OutlinedTextField(
            value = searchText,
            onValueChange = onSearchChange,
            label = { Text("Search games", color = Color(0xFF6EE7B7)) },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .weight(1f)
                .height(56.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF374151),
                focusedTextColor = Color(0xFFE5E7EB),
                unfocusedTextColor = Color(0xFF9CA3AF),
                cursorColor = Color(0xFF10B981),
                focusedContainerColor = Color(0xFF1B263B).copy(alpha = 0.5f),
                unfocusedContainerColor = Color(0xFF0D1B2A).copy(alpha = 0.5f)
            )
        )

        // Refresh button - fixed width, matching height
        RefreshButton(
            isRefreshing = isRefreshing,
            canRefresh = canRefresh,
            remainingSeconds = remainingSeconds,
            onClick = onRefreshClick,
            modifier = Modifier.width(120.dp).height(56.dp),
            showText = true
        )
    }
}