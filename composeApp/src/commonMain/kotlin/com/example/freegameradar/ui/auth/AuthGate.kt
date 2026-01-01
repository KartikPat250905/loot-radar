package com.example.freegameradar.ui.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.freegameradar.data.auth.AuthState
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
                // The Scaffold ensures the loading indicator has the correct dark background.
                Scaffold {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(it),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            AuthState.LoggedIn,
            AuthState.Guest -> {
                content()
            }

            is AuthState.Error -> {
                AuthScreen(
                    authViewModel = authViewModel
                )
            }
        }
    }
}