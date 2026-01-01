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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.freegameradar.permissions.rememberPermissionHandler
import com.example.freegameradar.ui.components.FilterChip
import com.example.freegameradar.ui.components.settings.SettingsItem
import com.example.freegameradar.ui.components.settings.SettingsSectionHeader
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.SettingsViewModel
import com.example.freegameradar.ui.viewmodel.UserPreferencesViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferencesState by userPreferencesViewModel.uiState.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmation by remember { mutableStateOf(false) }
    var showNotificationPreferenceDialog by remember { mutableStateOf(false) }

    val permissionHandler = rememberPermissionHandler()

    fun handleSignOut() {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
        viewModel.signOut()
    }

    fun handleDeleteAccount() {
        viewModel.deleteAccount { result ->
            if (result.isSuccess) {
                // Only navigate after the account has been successfully deleted.
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            }
            // Optionally, handle the failure case e.g., show a snackbar
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSectionHeader(title = "Account")

            if (uiState.user != null && !uiState.isGuest) {
                SettingsItem(
                    icon = Icons.Default.ExitToApp,
                    title = "Sign Out",
                    subtitle = "Sign out of your account",
                    onClick = { showSignOutConfirmation = true }
                )
            } else {
                SettingsItem(
                    icon = Icons.Default.Upgrade,
                    title = "Upgrade Account",
                    subtitle = "Create an account to save your data",
                    onClick = { showUpgradeDialog = true }
                )
            }

            SettingsItem(
                icon = Icons.Default.Delete,
                title = "Delete Account",
                subtitle = "Permanently delete your account",
                onClick = { showDeleteConfirmation = true }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SettingsSectionHeader(title = "Notifications")

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Enable Notifications")
                Switch(
                    checked = preferencesState.notificationsEnabled,
                    onCheckedChange = { wantsToEnable ->
                        if (wantsToEnable) {
                            permissionHandler.requestNotificationPermission { isGranted ->
                                userPreferencesViewModel.setNotificationsEnabled(isGranted)
                            }
                        } else {
                            userPreferencesViewModel.setNotificationsEnabled(false)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            PreferenceSummaryCard(
                platforms = preferencesState.preferredGamePlatforms,
                types = preferencesState.preferredGameTypes,
                onEditClick = { showNotificationPreferenceDialog = true },
            )
        }
    }

    if (showUpgradeDialog) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
        var confirmPassword by remember { mutableStateOf("") }
        var passwordVisibility by remember { mutableStateOf(false) }
        var confirmPasswordVisibility by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }


        AlertDialog(
            onDismissRequest = { if (!isLoading) showUpgradeDialog = false },
            title = { Text("Upgrade Account") },
            text = {
                Column {
                    Text("Enter your email and password to create an account. Your data will be saved.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        isError = error != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        readOnly = isLoading
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        isError = error != null,
                        visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        readOnly = isLoading,
                        trailingIcon = {
                            val image = if (passwordVisibility)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(imageVector = image, "toggle password visibility")
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        isError = error != null,
                        visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        readOnly = isLoading,
                        trailingIcon = {
                            val image = if (confirmPasswordVisibility)
                                Icons.Filled.Visibility
                            else Icons.Filled.VisibilityOff

                            IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                Icon(imageVector = image, "toggle password visibility")
                            }
                        }
                    )

                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (password != confirmPassword) {
                            error = "Passwords do not match."
                            return@Button
                        }
                        isLoading = true
                        error = null
                        viewModel.upgradeAccount(email, password) { result ->
                            isLoading = false
                            if (result.isSuccess) {
                                showUpgradeDialog = false
                            } else {
                                error = result.exceptionOrNull()?.message ?: "An unknown error occurred."
                            }
                        }
                    },
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Text("Upgrade")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isLoading) showUpgradeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
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
            title = { Text("Edit Notification Preferences") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text("Notify me about these platforms:", style = MaterialTheme.typography.titleMedium)
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
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationPreferenceDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            title = { Text("Sign Out") },
            text = { Text("Are you sure you want to sign out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        handleSignOut()
                        showSignOutConfirmation = false
                    }
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to permanently delete your account? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        handleDeleteAccount()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Current Preferences", style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Preferences")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Platforms: ${platforms.joinToString().ifEmpty { "None" }}")
            Spacer(modifier = Modifier.height(8.dp))
            Text("Types: ${types.joinToString().ifEmpty { "None" }}")
        }
    }
}
