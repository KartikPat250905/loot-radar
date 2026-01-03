package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.viewmodel.AuthViewModel

@Composable
fun SignUpScreen(
    authViewModel: AuthViewModel,
    onLoginClicked: () -> Unit,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisibility by remember { mutableStateOf(false) }
    var confirmPasswordVisibility by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val authState by authViewModel.authState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0D1B2A),
                        Color(0xFF1B263B),
                        Color(0xFF0D1B2A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val appNameText = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF6EE7B7)
                            )
                        ),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 32.sp
                    )
                ) {
                    append("FreeGameRadar")
                }
            }
            Text(text = appNameText)

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Create Account",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE5E7EB)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Sign up to get started",
                fontSize = 16.sp,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(40.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    localError = null
                },
                label = { Text("Email / Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedTextColor = Color(0xFFE5E7EB),
                    unfocusedTextColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF10B981),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedLabelColor = Color(0xFF9CA3AF)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    localError = null
                },
                label = { Text("Password") },
                visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedTextColor = Color(0xFFE5E7EB),
                    unfocusedTextColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF10B981),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedLabelColor = Color(0xFF9CA3AF)
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisibility = !passwordVisibility }) {
                        Icon(
                            imageVector = if (passwordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "toggle password visibility",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    localError = null
                },
                label = { Text("Confirm Password") },
                visualTransformation = if (confirmPasswordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = localError == "Passwords do not match",
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF374151),
                    focusedTextColor = Color(0xFFE5E7EB),
                    unfocusedTextColor = Color(0xFFE5E7EB),
                    cursorColor = Color(0xFF10B981),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedLabelColor = Color(0xFF9CA3AF),
                    errorBorderColor = Color(0xFFEF4444),
                    errorLabelColor = Color(0xFFEF4444)
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisibility = !confirmPasswordVisibility }) {
                        Icon(
                            imageVector = if (confirmPasswordVisibility) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = "toggle password visibility",
                            tint = Color(0xFF9CA3AF)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = authState) {
                is AuthState.Error -> {
                    if (!state.message.contains("not logged in", ignoreCase = true)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = state.message,
                                color = Color(0xFFEF4444),
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                else -> {}
            }

            if (localError != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0x33EF4444)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = localError ?: "",
                        color = Color(0xFFEF4444),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Button(
                onClick = {
                    if (password == confirmPassword) {
                        authViewModel.register(email, password)
                    } else {
                        localError = "Passwords do not match"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Sign Up",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "Already have an account?",
                    fontSize = 14.sp,
                    color = Color(0xFF9CA3AF)
                )
                TextButton(
                    onClick = onLoginClicked,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF10B981))
                ) {
                    Text(
                        "Login",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}