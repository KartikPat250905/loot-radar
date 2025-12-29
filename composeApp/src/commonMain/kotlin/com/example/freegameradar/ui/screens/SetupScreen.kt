package com.example.freegameradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.freegameradar.settings.UserSettings
import com.example.freegameradar.ui.components.FilterChip
import com.example.freegameradar.ui.viewmodel.SetupViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onNavigateToHome: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    var notificationsEnabled by remember { mutableStateOf(userSettings.notificationsEnabled) }
    var selectedPlatforms by remember { mutableStateOf(userSettings.preferredGamePlatforms) }
    var selectedTypes by remember { mutableStateOf(userSettings.preferredGameTypes) }

    val platforms = listOf(
        "pc", "steam", "epic-games-store", "ubisoft", "gog", "itchio",
        "ps4", "ps5", "xbox-one", "xbox-series-xs", "switch",
        "android", "ios", "vr", "battlenet", "origin", "drm-free", "xbox-360"
    )
    val types = listOf("game", "dlc", "early access")

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Personalize Your Experience", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Choose what you want to be notified about.", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))

            Text("Notify me about these platforms:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
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

            Text("Notify me about these types:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth())
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
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Notifications")
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = { notificationsEnabled = it }
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "You can change these preferences at any time in the Settings screen.",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveSettings(
                        UserSettings(
                            notificationsEnabled = notificationsEnabled,
                            preferredGamePlatforms = selectedPlatforms,
                            preferredGameTypes = selectedTypes
                        )
                    )
                    onNavigateToHome()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save and Continue")
            }
        }
    }
}
