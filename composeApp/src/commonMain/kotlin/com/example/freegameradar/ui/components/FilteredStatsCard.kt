package com.example.freegameradar.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.freegameradar.ui.viewmodel.FilteredStats

@Composable
fun FilteredStatsCard(filteredStats: FilteredStats) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Filtered Stats", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Text("Item Count: ", style = MaterialTheme.typography.bodyMedium)
                Text(filteredStats.count.toString(), style = MaterialTheme.typography.bodyLarge)
            }
            Row {
                Text("Total Worth: ", style = MaterialTheme.typography.bodyMedium)
                Text(String.format("$%.2f", filteredStats.totalWorth), style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}