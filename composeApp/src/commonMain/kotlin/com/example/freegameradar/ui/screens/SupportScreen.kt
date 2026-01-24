package com.example.freegameradar.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.freegameradar.ui.components.BackButton
import com.example.freegameradar.ui.components.ThemedSupportButton

// Support Screen
@Composable
fun SupportScreen(
    onNavigateToRedeem: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(Modifier.height(60.dp))

            Text(
                "☕ Support FreeGameRadar",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E7EB),
                letterSpacing = 0.5.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Keep this app free and ad-free for everyone!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF9CA3AF),
                letterSpacing = 0.3.sp
            )

            Spacer(Modifier.height(32.dp))

            // Ko-fi Card with theme
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF10B981),
                                Color(0xFF34D399),
                                Color(0xFF10B981)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1B263B),
                                    Color(0xFF0D1B2A)
                                )
                            ),
                            shape = RoundedCornerShape(15.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Text(
                            "💚 Support via Ko-fi",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFE5E7EB),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "100% goes directly to development",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF9CA3AF)
                        )

                        Spacer(Modifier.height(20.dp))

                        // Tiers
                        ThemedTierRow("Coffee Support", "$2.99", "30 days ad-free")
                        Spacer(Modifier.height(12.dp))
                        ThemedTierRow("Big Coffee", "$7.99", "90 days ad-free")
                        Spacer(Modifier.height(12.dp))
                        ThemedTierRow("Lifetime", "$19.99", "Forever ad-free")

                        Spacer(Modifier.height(24.dp))

                        // Ko-fi Button
                        ThemedSupportButton(
                            text = "Open Ko-fi",
                            icon = Icons.Default.Coffee,
                            onClick = {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://ko-fi.com/freegameradar")
                                )
                                context.startActivity(intent)
                            }
                        )

                        Spacer(Modifier.height(12.dp))

                        val annotatedString = buildAnnotatedString {
                            append("You'll receive a unique code via email or on kofi within 24 hours. You can mail at ")
                            pushStringAnnotation("EMAIL", "mailto:freegameradar.app@gmail.com")
                            withStyle(style = SpanStyle(color = Color(0xFF6EE7B7), textDecoration = TextDecoration.Underline)) {
                                append("freegameradar.app@gmail.com")
                            }
                            pop()
                            append(" for further inquires.")
                        }

                        ClickableText(
                            text = annotatedString,
                            style = TextStyle(fontSize = 12.sp, color = Color(0xFF6B7280)),
                            onClick = { offset ->
                                annotatedString.getStringAnnotations("EMAIL", offset, offset).firstOrNull()?.let {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse(it.item)
                                    }
                                    context.startActivity(intent)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // Redeem Button
            ThemedSupportButton(
                text = "Already Have a Code? Redeem Here",
                onClick = onNavigateToRedeem,
                isPrimary = false
            )
        }

        // Back Button
        Box(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            BackButton(onClick = onBack)
        }
    }
}

@Composable
private fun ThemedTierRow(name: String, price: String, benefit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF0D1B2A).copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE5E7EB),
                letterSpacing = 0.3.sp
            )
            Text(
                benefit,
                fontSize = 12.sp,
                color = Color(0xFF9CA3AF)
            )
        }
        Text(
            price,
            fontSize = 16.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF10B981)
        )
    }
}
