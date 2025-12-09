package com.example.lootradarkmp.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.lootradarkmp.ui.viewmodel.GameViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FilterBar(viewModel: GameViewModel) {

    var expanded by remember { mutableStateOf(false) }
    var platformSearch by remember { mutableStateOf("") }
    val filters by viewModel.filters.collectAsState()

    val platforms = listOf(
        "pc", "steam", "epic-games-store", "ubisoft", "gog", "itchio",
        "ps4", "ps5", "xbox-one", "xbox-series-xs", "switch",
        "android", "ios", "vr", "battlenet", "origin", "drm-free", "xbox-360"
    )

    val types = listOf("game", "dlc", "early access")

    val filteredPlatforms = remember(platformSearch) {
        if (platformSearch.isBlank()) platforms
        else platforms.filter { it.contains(platformSearch, ignoreCase = true) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Filters",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    if (filters.platforms.isNotEmpty() || filters.types.isNotEmpty()) {
                        Text(
                            text = "${filters.platforms.size + filters.types.size} active",
                            fontSize = 12.sp,
                            color = Color(0xFF64B5F6),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                TextButton(
                    onClick = { expanded = !expanded },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF64B5F6)
                    )
                ) {
                    Text(
                        text = if (expanded) "Collapse" else "Expand",
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            // Animated section
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .verticalScroll(rememberScrollState())
                ) {

                    // Platform section
                    Text(
                        text = "Platform",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B0B0),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = platformSearch,
                        onValueChange = { platformSearch = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        placeholder = { Text("Search platforms...", fontSize = 14.sp) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64B5F6)
                            )
                        },
                        trailingIcon = {
                            if (platformSearch.isNotEmpty()) {
                                IconButton(onClick = { platformSearch = "" }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        tint = Color(0xFFB0B0B0)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF64B5F6),
                            unfocusedBorderColor = Color(0xFF404040),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color(0xFF64B5F6)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredPlatforms.forEach { platform ->
                            FilterChip(
                                text = platform,
                                selected = filters.platforms.contains(platform),
                                onClick = { viewModel.togglePlatform(platform) }
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Type section
                    Text(
                        text = "Type",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB0B0B0),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            FilterChip(
                                text = type,
                                selected = filters.types.contains(type),
                                onClick = { viewModel.toggleType(type) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                    ) {

                        if (filters.platforms.isNotEmpty() || filters.types.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.clearFilters()
                                },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFB0B0B0)
                                )
                            ) {
                                Text("Clear All", fontSize = 14.sp)
                            }
                        }

                        Button(
                            onClick = { expanded = false },
                            modifier = Modifier.height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Text(
                                text = "Apply Filters",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
