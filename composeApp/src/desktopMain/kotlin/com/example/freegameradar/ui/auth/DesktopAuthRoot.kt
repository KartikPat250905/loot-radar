package com.example.freegameradar.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.freegameradar.App
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@Composable
fun DesktopAuthRoot(
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.authState.collectAsState()

    when (authState) {
        is AuthState.Loading -> {
            AppLoadingScreen()
        }
        is AuthState.Guest,
        is AuthState.LoggedIn -> {
            App(
                authViewModel = authViewModel,
                startRoute = Screen.Home.route
            )
        }
        else -> {
            DesktopLoginScreen(
                authState = authState,
                onSignInClick = { email, password ->
                    authViewModel.login(email, password)
                },
                onContinueAsGuest = {
                    authViewModel.continueAsGuest()
                },
                onGoToSignUp = {
                    // For now, you can call register directly or navigate to an auth flow screen
                }
            )
        }
    }
}