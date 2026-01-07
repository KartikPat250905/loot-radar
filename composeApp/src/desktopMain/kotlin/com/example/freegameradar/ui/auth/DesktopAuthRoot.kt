package com.example.freegameradar.ui.auth

import androidx.compose.runtime.*
import com.example.freegameradar.App
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.data.models.User
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.navigation.Screen
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@Composable
fun DesktopAuthRoot(
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()

    var showSignUp by remember { mutableStateOf(false) }

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
            if (showSignUp) {
                DesktopSignUpScreen(
                    authState = authState,
                    onSignUpClick = { email, password ->
                        authViewModel.register(email, password)
                    },
                    onGoToLogin = {
                        showSignUp = false
                    }
                )
            } else {
                DesktopLoginScreen(
                    authState = authState,
                    onSignInClick = { email, password ->
                        authViewModel.login(email, password)
                    },
                    onContinueAsGuest = {
                        authViewModel.continueAsGuest()
                    },
                    onGoToSignUp = {
                        showSignUp = true
                    }
                )
            }
        }
    }
}