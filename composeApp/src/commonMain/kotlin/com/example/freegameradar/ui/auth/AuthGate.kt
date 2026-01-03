package com.example.freegameradar.ui.auth

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.screens.AuthScreen
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthGate(
    authViewModel: AuthViewModel,
    content: @Composable () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    ModernDarkTheme {
        when (authState) {
            AuthState.Loading -> {
                AppLoadingScreen()
            }

            AuthState.LoggedIn, AuthState.Guest -> {
                content()
            }
            is AuthState.Success,
            is AuthState.Error -> {
                AuthScreen(
                    authViewModel = authViewModel
                )
            }
        }
    }
}