package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.data.repository.validateAndRedeemCode
import com.example.freegameradar.ui.components.BackButton
import com.example.freegameradar.ui.components.ThemedSupportButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RedeemCodeScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var code by remember { mutableStateOf("") }
    var isValidating by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(80.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎉 Enter Your Code",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFE5E7EB),
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    "Enter the code from your Ko-fi email",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF9CA3AF)
                )

                Spacer(Modifier.height(32.dp))

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase().trim()
                        errorMessage = null
                    },
                    label = { Text("Code (e.g., RADAR-TEST1)", color = Color(0xFF6EE7B7)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorMessage != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedTextColor = Color(0xFFE5E7EB),
                        unfocusedTextColor = Color(0xFF9CA3AF),
                        cursorColor = Color(0xFF10B981),
                        errorBorderColor = Color(0xFFEF4444),
                        focusedContainerColor = Color(0xFF1B263B).copy(alpha = 0.5f),
                        unfocusedContainerColor = Color(0xFF0D1B2A).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        errorMessage!!,
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (successMessage != null) {
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF10B981).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Text(
                            successMessage!!,
                            color = Color(0xFF10B981),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                ThemedSupportButton(
                    text = if (isValidating) "Validating..." else "Redeem Code",
                    onClick = {
                        scope.launch {
                            isValidating = true
                            errorMessage = null
                            successMessage = null

                            val result = validateAndRedeemCode(code)

                            if (result.success) {
                                successMessage = result.message
                                kotlinx.coroutines.delay(2000)
                                onSuccess()
                            } else {
                                errorMessage = result.message
                            }

                            isValidating = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = !isValidating
                )

                Spacer(Modifier.height(80.dp))
            }
        }

        // Back Button - MUST be after Column to render on top
        BackButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
    }
}
