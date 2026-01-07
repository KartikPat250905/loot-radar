package com.example.freegameradar.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.ui.components.AppLoadingScreen

@Composable
fun DesktopLoginScreen(
    authState: AuthState,
    onSignInClick: (email: String, password: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onGoToSignUp: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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
            )
    ) {
        // Show loading screen when authenticating
        AnimatedVisibility(
            visible = authState is AuthState.Loading,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            AppLoadingScreen()
        }

        // Subtle static glow effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(bottom = 48.dp)
            ) {
                val titleText = buildAnnotatedString {
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
                            fontSize = 48.sp
                        )
                    ) {
                        append("Free")
                    }
                    withStyle(
                        style = SpanStyle(
                            color = Color(0xFF6EE7B7),
                            fontWeight = FontWeight.Bold,
                            fontSize = 48.sp
                        )
                    ) {
                        append("Game")
                    }
                    withStyle(
                        style = SpanStyle(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF34D399),
                                    Color(0xFF10B981)
                                )
                            ),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 48.sp
                        )
                    ) {
                        append("Radar")
                    }
                }

                Text(text = titleText)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Welcome Back",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gradient accent line
                Box(
                    modifier = Modifier
                        .width(150.dp)
                        .height(3.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF10B981),
                                    Color(0xFF34D399),
                                    Color(0xFF10B981),
                                    Color.Transparent
                                )
                            ),
                            shape = RoundedCornerShape(50)
                        )
                )
            }

            // Login Card
            Box(
                modifier = Modifier
                    .width(500.dp)
            ) {
                // Card with gradient border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF0D1B2A),
                                    Color(0xFF1B263B),
                                    Color(0xFF0D1B2A)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF10B981).copy(alpha = 0.15f),
                                        Color(0xFF34D399).copy(alpha = 0.1f),
                                        Color(0xFF10B981).copy(alpha = 0.15f)
                                    )
                                ),
                                shape = RoundedCornerShape(19.dp)
                            )
                            .padding(1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF1B263B),
                                            Color(0xFF0D1B2A)
                                        )
                                    ),
                                    shape = RoundedCornerShape(18.dp)
                                )
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Email Field
                            ThemedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = "Email Address",
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Password Field
                            ThemedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = "Password",
                                isPassword = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Sign In Button
                            ThemedButton(
                                text = "Sign In",
                                onClick = { onSignInClick(email.trim(), password) },
                                enabled = authState !is AuthState.Loading &&
                                        email.isNotBlank() &&
                                        password.isNotBlank(),
                                isLoading = authState is AuthState.Loading,
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Create Account Button
                            ThemedButton(
                                text = "Create Account",
                                onClick = onGoToSignUp,
                                enabled = authState !is AuthState.Loading,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Guest Button
                            ThemedButton(
                                text = "Continue as Guest",
                                onClick = onContinueAsGuest,
                                enabled = authState !is AuthState.Loading,
                                isPrimary = false,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Error/Success Messages
                            when (authState) {
                                is AuthState.Error -> {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    MessageBox(
                                        message = authState.message,
                                        isError = true
                                    )
                                }
                                is AuthState.Success -> {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    MessageBox(
                                        message = authState.message,
                                        isError = false
                                    )
                                }
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF6EE7B7),
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF10B981),
                unfocusedBorderColor = Color(0xFF374151),
                focusedTextColor = Color(0xFFE5E7EB),
                unfocusedTextColor = Color(0xFF9CA3AF),
                cursorColor = Color(0xFF10B981),
                focusedContainerColor = Color(0xFF1B263B).copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFF0D1B2A).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun ThemedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isPrimary: Boolean,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.height(56.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (isPrimary && enabled) {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF10B981)
                            )
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF374151).copy(alpha = if (enabled) 0.5f else 0.2f),
                                Color(0xFF1F2937).copy(alpha = if (enabled) 0.5f else 0.2f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(if (isPrimary && enabled) 2.dp else 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = if (isPrimary && enabled) {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1B263B),
                                    Color(0xFF0D1B2A)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF0D1B2A),
                                    Color(0xFF1B263B)
                                )
                            )
                        },
                        shape = RoundedCornerShape(13.dp)
                    )
                    .clip(RoundedCornerShape(13.dp))
                    .clickable(
                        enabled = enabled,
                        onClick = onClick,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!isLoading) {
                    Text(
                        text = text,
                        fontWeight = if (isPrimary) FontWeight.ExtraBold else FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = if (enabled) {
                            if (isPrimary) Color(0xFF6EE7B7) else Color(0xFF9CA3AF)
                        } else {
                            Color(0xFF4B5563)
                        },
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBox(
    message: String,
    isError: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isError) {
                    Color(0xFFEF4444).copy(alpha = 0.1f)
                } else {
                    Color(0xFF10B981).copy(alpha = 0.1f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isError) {
                Color(0xFFEF4444)
            } else {
                Color(0xFF10B981)
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}