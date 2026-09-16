package com.radarlabs.freegameradar.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = Color(0xFF1B263B).copy(alpha = 0.95f), // Explicit color that matches your theme
        shadowElevation = 4.dp,
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(
                onClick = onClick,
                indication = androidx.compose.material3.ripple(bounded = true),
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        Icon(
            imageVector = Icons.Filled.ArrowBack,
            contentDescription = "Go back",
            tint = Color(0xFFE5E7EB), // Explicit light color for visibility
            modifier = Modifier.padding(12.dp)
        )
    }
}
