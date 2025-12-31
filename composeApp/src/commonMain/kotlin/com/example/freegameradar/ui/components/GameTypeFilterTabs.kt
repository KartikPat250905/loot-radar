package com.example.freegameradar.ui.components

import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.freegameradar.ui.viewmodel.GameTypeFilter

@Composable
fun GameTypeFilterTabs(
    selectedFilter: GameTypeFilter,
    onFilterSelected: (GameTypeFilter) -> Unit
) {
    val filters = GameTypeFilter.values()

    TabRow(selectedTabIndex = filters.indexOf(selectedFilter)) {
        filters.forEach { filter ->
            Tab(
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) },
                text = { Text(filter.toDisplayString()) }
            )
        }
    }
}
