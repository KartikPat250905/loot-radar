package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.freegameradar.permissions.rememberPermissionHandler
import com.example.freegameradar.ui.components.FilterChip
import com.example.freegameradar.ui.components.settings.SettingsItem
import com.example.freegameradar.ui.components.settings.SettingsSectionHeader
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    userPreferencesViewModel: UserPreferencesViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val preferencesState by userPreferencesViewModel.uiState.collectAsState()
    var showNotificationPreferenceDialog by remember { mutableStateOf(false) }

    val permissionHandler = rememberPermissionHandler()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B),
                        Color(0xFF0D1B2A)
                    )
                )
            )
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE5E7EB),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSectionHeader(title = "Notifications")

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1B263B)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Enable Notifications",
                    color = Color(0xFFE5E7EB),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Switch(
                    checked = preferencesState.notificationsEnabled,
                    onCheckedChange = { wantsToEnable ->
                        userPreferencesViewModel.setNotificationsEnabled(wantsToEnable)
                        if (wantsToEnable) {
                            permissionHandler.requestNotificationPermission { isGranted ->
                                if(!isGranted) {
                                    userPreferencesViewModel.setNotificationsEnabled(false)
                                }
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = Color(0xFF10B981),
                        checkedBorderColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFF374151),
                        uncheckedBorderColor = Color(0xFF4B5563)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        PreferenceSummaryCard(
            platforms = preferencesState.preferredGamePlatforms,
            types = preferencesState.preferredGameTypes,
            onEditClick = { showNotificationPreferenceDialog = true },
        )

        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader(title = "About")
        SettingsItem(
            icon = Icons.Default.Info,
            title = "About App",
            subtitle = "View app information and credits",
            onClick = { navController.navigate(Screen.About.route) }
        )

        Spacer(modifier = Modifier.height(80.dp))
    }

    if (showNotificationPreferenceDialog) {
        val platforms = listOf(
            "pc", "steam", "epic-games-store", "ubisoft", "gog", "itchio",
            "ps4", "ps5", "xbox-one", "xbox-series-xs", "switch",
            "android", "ios", "vr", "battlenet", "origin", "drm-free", "xbox-360"
        )
        val types = listOf("game", "dlc", "early access")

        var selectedPlatforms by remember { mutableStateOf(preferencesState.preferredGamePlatforms) }
        var selectedTypes by remember { mutableStateOf(preferencesState.preferredGameTypes) }

        AlertDialog(
            onDismissRequest = { showNotificationPreferenceDialog = false },
            containerColor = Color(0xFF1B263B),
            title = { Text("Edit Notification Preferences", color = Color(0xFFE5E7EB)) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Notify me about these platforms:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        platforms.forEach { platform ->
                            FilterChip(
                                text = platform,
                                selected = selectedPlatforms.contains(platform),
                                onClick = {
                                    selectedPlatforms = if (selectedPlatforms.contains(platform)) {
                                        selectedPlatforms - platform
                                    } else {
                                        selectedPlatforms + platform
                                    }
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        "Notify me about these types:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFE5E7EB)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.forEach { type ->
                            FilterChip(
                                text = type,
                                selected = selectedTypes.contains(type),
                                onClick = {
                                    selectedTypes = if (selectedTypes.contains(type)) {
                                        selectedTypes - type
                                    } else {
                                        selectedTypes + type
                                    }
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userPreferencesViewModel.updatePreferences(
                            preferredGamePlatforms = selectedPlatforms,
                            preferredGameTypes = selectedTypes
                        )
                        showNotificationPreferenceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showNotificationPreferenceDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PreferenceSummaryCard(
    platforms: List<String>,
    types: List<String>,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1B263B)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Current Preferences",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFE5E7EB)
                )
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit Preferences",
                        tint = Color(0xFF10B981)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Platforms: ${platforms.joinToString().ifEmpty { "None" }}",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Types: ${types.joinToString().ifEmpty { "None" }}",
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )
        }
    }
}