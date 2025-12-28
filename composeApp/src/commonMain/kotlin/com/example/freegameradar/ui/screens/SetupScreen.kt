package com.example.freegameradar.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import com.example.freegameradar.settings.UserSettings
import com.example.freegameradar.ui.viewmodel.SetupViewModel

@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onNavigateToHome: () -> Unit
) {
    val userSettings by viewModel.userSettings.collectAsState()
    var platform by remember { mutableStateOf(userSettings.preferredPlatform) }
    var currency by remember { mutableStateOf(userSettings.currency) }
    var notificationsEnabled by remember { mutableStateOf(userSettings.notificationsEnabled) }

    Scaffold {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Welcome to Loot Radar!", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Let's get you set up.", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = platform,
                onValueChange = { platform = it },
                label = { Text("Preferred Platform") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = currency,
                onValueChange = { currency = it },
                label = { Text("Currency") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

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

            Button(
                onClick = {
                    viewModel.saveSettings(
                        UserSettings(
                            preferredPlatform = platform,
                            currency = currency,
                            notificationsEnabled = notificationsEnabled
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
