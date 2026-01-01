package com.example.freegameradar.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.example.freegameradar.ui.components.settings.SettingsItem
import com.example.freegameradar.ui.components.settings.SettingsSectionHeader
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }

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
                    onClick = { handleSignOut() }
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
        }
    }

    if (showUpgradeDialog) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }
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
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        readOnly = isLoading
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
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
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
