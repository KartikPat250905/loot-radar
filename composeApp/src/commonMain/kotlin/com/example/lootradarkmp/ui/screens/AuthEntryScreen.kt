package com.example.lootradarkmp.ui.screens

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import com.example.lootradarkmp.data.auth.AuthState
import com.example.lootradarkmp.ui.viewmodel.AuthViewModel

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

        is AuthState.Guest -> {
            LoginScreen(authViewModel)
        }

        is AuthState.Error -> {
            LoginScreen(
                authViewModel,
                error = (authState as AuthState.Error).message
            )
        }
    }
}
