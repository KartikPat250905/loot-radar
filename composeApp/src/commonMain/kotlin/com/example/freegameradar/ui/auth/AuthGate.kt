package com.example.freegameradar.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.screens.LoginScreen
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@Composable
fun AuthGate(
    authViewModel: AuthViewModel,
    content: @Composable () -> Unit
) {
    val authState by authViewModel.authState.collectAsState()

    ModernDarkTheme {
        when (val state = authState) {
            AuthState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            AuthState.LoggedIn,
            AuthState.Guest -> {
                content()
            }

            is AuthState.Error -> {
                LoginScreen(
                    authViewModel = authViewModel,
                    error = state.message
                )
            }
        }
    }
}
