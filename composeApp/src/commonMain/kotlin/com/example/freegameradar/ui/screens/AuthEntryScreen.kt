package com.example.freegameradar.ui.screens

import androidx.compose.runtime.*
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@Composable
fun AuthEntryScreen(
    authViewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    when (authState) {

        is AuthState.Loading -> {
            AppLoadingScreen()
        }

        is AuthState.LoggedIn -> {
            LaunchedEffect(Unit) {
                onAuthenticated()
            }
        }

        is AuthState.Guest,
        is AuthState.Error,
        is AuthState.Success -> {
            AuthScreen(authViewModel)
        }
    }
}
