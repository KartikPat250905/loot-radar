package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.ui.viewmodel.GameTypeFilter

@Composable
fun GameTypeFilterTabs(
    selectedFilter: GameTypeFilter,
    onFilterSelected: (GameTypeFilter) -> Unit
) {
    val filters = GameTypeFilter.values()

    TabRow(
        selectedTabIndex = filters.indexOf(selectedFilter),
        containerColor = Color(0xFF0D1B2A),
        contentColor = Color(0xFF6EE7B7),
        indicator = { tabPositions ->
            if (filters.indexOf(selectedFilter) < tabPositions.size) {
                Box(
                    Modifier
                        .tabIndicatorOffset(tabPositions[filters.indexOf(selectedFilter)])
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF10B981),
                                    Color(0xFF34D399),
                                    Color(0xFF10B981)
                                )
                            ),
                            shape = RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp)
                        )
                )
            }
        },
        divider = {}
    ) {
        filters.forEach { filter ->
            Tab(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                text = {
                    Text(
                        text = filter.toDisplayString(),
                        fontWeight = if (filter == selectedFilter) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp,
                        color = if (filter == selectedFilter) Color(0xFF10B981) else Color(0xFF6B7280)
                    )
                }
            )
        }
    }
}
