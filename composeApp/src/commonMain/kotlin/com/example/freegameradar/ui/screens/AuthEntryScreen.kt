package com.example.freegameradar.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@Composable
fun AuthEntryScreen(
    authViewModel: AuthViewModel,
    onAuthenticated: () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    when (authState) {

        is AuthState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
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
