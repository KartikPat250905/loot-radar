package com.radarlabs.freegameradar.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * App-wide snackbar host styled to match the navy/emerald theme.
 * Red border = error. Pass an emerald borderColor for success messages.
 */
@Composable
fun ThemedSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    borderColor: Color = Color(0xFFEF4444),
    bottomPadding: Dp = 16.dp
) {
    SnackbarHost(hostState, modifier = modifier) { data ->
        Snackbar(
            snackbarData = data,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = bottomPadding)
                .border(
                    width = 1.dp,
                    color = borderColor.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            containerColor = Color(0xFF1B263B),
            contentColor = Color(0xFFE5E7EB),
            actionContentColor = Color(0xFF10B981),
            dismissActionContentColor = Color(0xFF9CA3AF)
        )
    }
}
