package com.example.freegameradar.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.viewmodel.AuthViewModel


@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
) {
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()

    NavHost(navController = navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                authViewModel = authViewModel,
                onSignUpClicked = { navController.navigate("signup") },
                error = (authState as? AuthState.Error)?.message
            )
        }
        composable("signup") {
            SignUpScreen(
                authViewModel = authViewModel,
                onLoginClicked = { navController.navigate("login") },
                error = (authState as? AuthState.Error)?.message
            )
        }
    }
}