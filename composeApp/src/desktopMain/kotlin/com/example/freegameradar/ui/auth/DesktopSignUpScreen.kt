package com.example.freegameradar.ui.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.auth.AuthState
import com.example.freegameradar.firebase.FirebaseErrorMapper
import com.example.freegameradar.ui.components.AppLoadingScreen
import com.example.freegameradar.ui.validation.ValidationUtils

@Composable
fun DesktopSignUpScreen(
    authState: AuthState,
    onSignUpClick: (email: String, password: String) -> Unit,
    onGoToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

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
                    text = "Create Your Account",
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

            // SignUp Card
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
                            SignUpThemedTextField(
                                value = email,
                                onValueChange = {
                                    email = it
                                    emailError = null
                                },
                                label = "Email Address",
                                errorMessage = emailError,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Password Field
                            SignUpThemedTextField(
                                value = password,
                                onValueChange = {
                                    password = it
                                    passwordError = null
                                },
                                label = "Password (6+ characters)",
                                errorMessage = passwordError,
                                isPassword = true,
                                showPassword = showPassword,
                                onTogglePasswordVisibility = { showPassword = !showPassword },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Confirm Password Field
                            SignUpThemedTextField(
                                value = confirmPassword,
                                onValueChange = {
                                    confirmPassword = it
                                    confirmPasswordError = null
                                },
                                label = "Confirm Password",
                                errorMessage = confirmPasswordError,
                                isPassword = true,
                                showPassword = showConfirmPassword,
                                onTogglePasswordVisibility = { showConfirmPassword = !showConfirmPassword },
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Firebase error message
                            if (firebaseError != null) {
                                Spacer(modifier = Modifier.height(16.dp))
                                SignUpMessageBox(
                                    message = firebaseError!!,
                                    isError = true
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            // Create Account Button
                            SignUpThemedButton(
                                text = "Create Account",
                                onClick = {
                                    emailError = ValidationUtils.getEmailError(email)
                                    passwordError = ValidationUtils.getPasswordError(password)

                                    // Validate confirm password
                                    confirmPasswordError = when {
                                        confirmPassword.isEmpty() -> "Please confirm your password"
                                        confirmPassword != password -> "Passwords do not match"
                                        else -> null
                                    }

                                    if (emailError == null && passwordError == null && confirmPasswordError == null) {
                                        onSignUpClick(email.trim(), password)
                                    }
                                },
                                enabled = authState !is AuthState.Loading,
                                isPrimary = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // Already have account link
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(
                                        onClick = onGoToLogin,
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    )
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Already have an account? ",
                                    fontSize = 14.sp,
                                    color = Color(0xFF9CA3AF)
                                )
                                Text(
                                    text = "Sign In",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981),
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SignUpThemedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    errorMessage: String? = null,
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePasswordVisibility: (() -> Unit)? = null,
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
            visualTransformation = if (isPassword && !showPassword) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            trailingIcon = if (isPassword && onTogglePasswordVisibility != null) {
                {
                    IconButton(onClick = onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (showPassword) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = if (showPassword) "Hide password" else "Show password",
                            tint = Color(0xFF6B7280)
                        )
                    }
                }
            } else null,
            singleLine = true,
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (errorMessage != null) Color(0xFFEF4444) else Color(0xFF10B981),
                unfocusedBorderColor = if (errorMessage != null) Color(0xFFEF4444).copy(alpha = 0.5f) else Color(0xFF374151),
                focusedTextColor = Color(0xFFE5E7EB),
                unfocusedTextColor = Color(0xFF9CA3AF),
                cursorColor = Color(0xFF10B981),
                focusedContainerColor = Color(0xFF1B263B).copy(alpha = 0.3f),
                unfocusedContainerColor = Color(0xFF0D1B2A).copy(alpha = 0.3f),
                errorBorderColor = Color(0xFFEF4444),
                errorTextColor = Color(0xFFE5E7EB),
                errorContainerColor = Color(0xFF1B263B).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Error message
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                fontSize = 12.sp,
                color = Color(0xFFEF4444),
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun SignUpThemedButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean,
    isPrimary: Boolean,
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

@Composable
private fun SignUpMessageBox(
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