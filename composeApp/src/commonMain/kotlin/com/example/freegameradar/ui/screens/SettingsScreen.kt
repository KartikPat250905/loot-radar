package com.radarlabs.freegameradar.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.radarlabs.freegameradar.ui.components.FilterChip
import com.radarlabs.freegameradar.ui.components.settings.SettingsItem
import com.radarlabs.freegameradar.ui.components.settings.SettingsSectionHeader
import com.radarlabs.freegameradar.ui.navigation.Screen
import com.radarlabs.freegameradar.ui.viewmodel.SettingsViewModel
import com.radarlabs.freegameradar.ui.viewmodel.UserPreferencesViewModel
import com.radarlabs.freegameradar.util.PermissionRequestResult
import com.radarlabs.freegameradar.util.isNotificationPermissionGranted
import com.radarlabs.freegameradar.util.openAppSettings
import com.radarlabs.freegameradar.util.rememberPermissionRequestLauncher
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    userPreferencesViewModel: UserPreferencesViewModel,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onSettingsScreenOpen: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferencesState by userPreferencesViewModel.uiState.collectAsState()

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showUpgradeDialog by remember { mutableStateOf(false) }
    var showSignOutConfirmation by remember { mutableStateOf(false) }
    var showNotificationPreferenceDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }

    var permissionGranted by remember { mutableStateOf(false) }

    val currentPermissionState = isNotificationPermissionGranted()

    LaunchedEffect(currentPermissionState) {
        permissionGranted = currentPermissionState
    }

    val permissionLauncher = rememberPermissionRequestLauncher { result ->
        when (result) {
            PermissionRequestResult.GRANTED -> {
                permissionGranted = true
                userPreferencesViewModel.setNotificationsEnabled(true)
            }
            PermissionRequestResult.DENIED -> {
                permissionGranted = false
                userPreferencesViewModel.setNotificationsEnabled(false)
            }
            PermissionRequestResult.PERMANENTLY_DENIED -> {
                permissionGranted = false
                userPreferencesViewModel.setNotificationsEnabled(false)
                showPermissionDeniedDialog = true
            }
        }
    }

    LaunchedEffect(key1 = "settings_open_tracker") {
        Log.d("SettingsScreen", "🟢 Screen opened, incrementing counter")
        viewModel.onSettingsScreenOpen()
    }

    LaunchedEffect(key1 = "settings_ad_listener") {
        Log.d("SettingsScreen", "👂 Listening for ad triggers")
        viewModel.showSettingsAd.collectLatest {
            Log.d("SettingsScreen", "📢 Ad trigger received, calling MainActivity callback")
            onSettingsScreenOpen()
        }
    }

    fun handleSignOut() {
        navController.navigate(Screen.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
        }
        viewModel.signOut()
    }

    fun handleDeleteAccount() {
        viewModel.deleteAccount { result ->
            if (result.isSuccess) {
                navController.navigate(Screen.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            }
        }
    }

    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("Permission Required") },
            text = { Text("Notifications are permanently disabled. To re-enable them, you must go to your phone's settings.") },
            confirmButton = {
                TextButton(onClick = { openAppSettings() }) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

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
            .padding(top = 16.dp)
    ) {
        Text(
            "Settings",
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFE5E7EB),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        Spacer(Modifier.height(16.dp))

        Box(Modifier.padding(horizontal = 16.dp)) {
            AdFreeStatusCard()
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { navController.navigate("support") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("☕ Support the Developer")
        }

        Spacer(Modifier.height(16.dp))

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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B))
        ) {
            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (permissionGranted) {
                                    userPreferencesViewModel.setNotificationsEnabled(true)
                                } else {
                                    permissionLauncher()
                                }
                            } else {
                                userPreferencesViewModel.setNotificationsEnabled(false)
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

                if (preferencesState.notificationsEnabled && !permissionGranted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x33EF4444), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Warning",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Permission Required",
                                color = Color(0xFFEF4444),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Notifications are enabled but this device needs permission.",
                                color = Color(0xFF9CA3AF),
                                fontSize = 12.sp
                            )
                        }
                        TextButton(onClick = { permissionLauncher() }) {
                            Text("Grant", color = Color(0xFF10B981), fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        PreferenceSummaryCard(
            platforms = preferencesState.preferredGamePlatforms,
            types = preferencesState.preferredGameTypes,
            onEditClick = { showNotificationPreferenceDialog = true }
        )

        Spacer(modifier = Modifier.height(16.dp))
        SettingsSectionHeader(title = "About")
        SettingsItem(
            icon = Icons.Default.Info,
            title = "About App",
            subtitle = "View app information and credits",
            onClick = { navController.navigate(Screen.About.route) }
        )
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
            containerColor = Color(0xFF1B263B),
            title = { Text("Upgrade Account", color = Color(0xFFE5E7EB)) },
            text = {
                Column {
                    Text(
                        "Enter your email and password to create an account. Your data will be saved.",
                        color = Color(0xFF9CA3AF)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        isError = error != null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        readOnly = isLoading,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFE5E7EB),
                            unfocusedTextColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981),
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        )
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFE5E7EB),
                            unfocusedTextColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981),
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                                Icon(
                                    imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "toggle password visibility",
                                    tint = Color(0xFF9CA3AF)
                                )
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF10B981),
                            unfocusedBorderColor = Color(0xFF374151),
                            focusedTextColor = Color(0xFFE5E7EB),
                            unfocusedTextColor = Color(0xFFE5E7EB),
                            cursorColor = Color(0xFF10B981),
                            focusedLabelColor = Color(0xFF10B981),
                            unfocusedLabelColor = Color(0xFF9CA3AF)
                        ),
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                                Icon(
                                    imageVector = if (confirmPasswordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                    contentDescription = "toggle password visibility",
                                    tint = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    )
                    error?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Color(0xFFEF4444))
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
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text("Upgrade")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { if (!isLoading) showUpgradeDialog = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showNotificationPreferenceDialog) {
        val platforms = listOf(
            "pc", "steam", "epic-games-store", "ubisoft", "gog", "itchio",
            "ps4", "ps5", "xbox-one", "xbox-series-xs", "switch", "android",
            "ios", "vr", "battlenet", "origin", "drm-free", "xbox-360"
        )
        val types = listOf("game", "dlc", "early access")
        var selectedPlatforms by remember { mutableStateOf(preferencesState.preferredGamePlatforms) }
        var selectedTypes by remember { mutableStateOf(preferencesState.preferredGameTypes) }

        AlertDialog(
            onDismissRequest = { showNotificationPreferenceDialog = false },
            containerColor = Color(0xFF1B263B),
            title = { Text("Edit Notification Preferences", color = Color(0xFFE5E7EB)) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
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
                                    selectedPlatforms = if (selectedPlatforms.contains(platform))
                                        selectedPlatforms - platform
                                    else
                                        selectedPlatforms + platform
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
                                    selectedTypes = if (selectedTypes.contains(type))
                                        selectedTypes - type
                                    else
                                        selectedTypes + type
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

    if (showSignOutConfirmation) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmation = false },
            containerColor = Color(0xFF1B263B),
            title = { Text("Sign Out", color = Color(0xFFE5E7EB)) },
            text = { Text("Are you sure you want to sign out?", color = Color(0xFF9CA3AF)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        handleSignOut()
                        showSignOutConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = Color(0xFF1B263B),
            title = { Text("Delete Account", color = Color(0xFFE5E7EB)) },
            text = {
                Text(
                    "Are you sure you want to permanently delete your account? This action cannot be undone.",
                    color = Color(0xFF9CA3AF)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        handleDeleteAccount()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFEF4444))
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF9CA3AF))
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun PreferenceSummaryCard(platforms: List<String>, types: List<String>, onEditClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
