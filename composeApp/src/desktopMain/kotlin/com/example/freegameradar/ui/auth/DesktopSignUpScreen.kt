package com.example.freegameradar.ui.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.firebase.FirebaseErrorMapper
import com.example.freegameradar.ui.validation.ValidationUtils

@Composable
fun DesktopSignUpScreen(
    authState: AuthState,
    onSignUpClick: (email: String, password: String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }

    // Handle Firebase errors
    val firebaseError by remember(authState) {
        derivedStateOf {
            when (authState) {
                is AuthState.Error -> FirebaseErrorMapper.mapException(
                    RuntimeException(authState.message)
                )
                else -> null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Create Account",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            isError = emailError != null,
            supportingText = {
                emailError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(0.4f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (6+ characters)") },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            isError = passwordError != null,
            supportingText = {
                passwordError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            },
            modifier = Modifier.fillMaxWidth(0.4f)
        )

        firebaseError?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                emailError = ValidationUtils.getEmailError(email)
                passwordError = ValidationUtils.getPasswordError(password)
                if (emailError == null && passwordError == null) {
                    onSignUpClick(email.trim(), password)
                }
            },
            enabled = authState !is AuthState.Loading,
            modifier = Modifier.fillMaxWidth(0.4f)
        ) {
            if (authState is AuthState.Loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    Text("Creating...")
                }
            } else {
                Text("Create Account")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onGoToLogin,
            modifier = Modifier.fillMaxWidth(0.4f)
        ) {
            Text("Already have an account? Sign In")
        }
    }
}