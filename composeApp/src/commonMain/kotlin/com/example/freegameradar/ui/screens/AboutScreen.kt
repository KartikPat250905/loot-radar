package com.example.freegameradar.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Column(
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
        // Custom top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF10B981)
                )
            }
            Text(
                text = "About",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFE5E7EB),
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            AboutSection()
            Spacer(modifier = Modifier.height(24.dp))
            AppVersionSection()
            Spacer(modifier = Modifier.height(24.dp))
            PrivacyPolicySection()
            Spacer(modifier = Modifier.height(24.dp))
            TermsOfServiceSection()
            Spacer(modifier = Modifier.height(24.dp))
            ContactSection()
            Spacer(modifier = Modifier.height(24.dp))
            AppInfoFooter()
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun AboutSection() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "About FreeGameRadar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "FreeGameRadar is a cross-platform application built with Kotlin Multiplatform that helps gamers discover legitimate free game offers from popular platforms such as Epic Games Store, Steam, GOG, and more.",
                fontSize = 14.sp,
                color = Color(0xFFE5E7EB),
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            val annotatedText = buildAnnotatedString {
                append("The app aggregates publicly available giveaway data from trusted third-party sources, including the GamerPower API (")
                pushStringAnnotation(tag = "URL", annotation = "https://www.gamerpower.com")
                withStyle(style = SpanStyle(color = Color(0xFF10B981), textDecoration = TextDecoration.Underline)) {
                    append("https://www.gamerpower.com")
                }
                pop()
                append("), and notifies users when new free games become available — so you never miss a deal.")
            }
            ClickableText(
                text = annotatedText,
                style = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color(0xFFE5E7EB), lineHeight = 20.sp),
                onClick = { offset ->
                    annotatedText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "FreeGameRadar supports Guest Mode (no account required) as well as an optional account system for users who want personalized notifications and preferences.",
                fontSize = 14.sp,
                color = Color(0xFFE5E7EB),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AppVersionSection() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "App Version",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("v1.0.0", fontSize = 16.sp, color = Color(0xFFE5E7EB), fontWeight = FontWeight.SemiBold)
            Text("(Kotlin Multiplatform)", fontSize = 13.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
fun PrivacyPolicySection() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Privacy Policy",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("FreeGameRadar respects your privacy.", fontSize = 14.sp, color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))

            Text("What we collect", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• No personal data is required in Guest Mode", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• If you create an account, we may collect:", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("  - Email address (for authentication and notifications)", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("  - App preferences (notification settings, platform filters)", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("What we do NOT collect", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• No payment information", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• No sensitive personal data", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• No cross-app or cross-website tracking", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("Third-Party Services", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            val annotatedServiceText = buildAnnotatedString {
                append("• Game giveaway data is provided by the GamerPower API (")
                pushStringAnnotation(tag = "URL", annotation = "https://www.gamerpower.com")
                withStyle(style = SpanStyle(color = Color(0xFF10B981), textDecoration = TextDecoration.Underline)) {
                    append("https://www.gamerpower.com")
                }
                pop()
                append(")")
            }
            ClickableText(
                text = annotatedServiceText,
                style = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color(0xFF9CA3AF)),
                onClick = { offset ->
                    annotatedServiceText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "• FreeGameRadar only displays publicly available information and does not own or control third-party content",
                fontSize = 13.sp,
                color = Color(0xFF9CA3AF)
            )

            Spacer(modifier = Modifier.height(12.dp))
            Text("Data Usage", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• Data is used solely to provide app functionality", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• Data is never sold or shared for advertising purposes", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("Contact", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("For privacy-related questions, contact:", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            val annotatedEmailText = buildAnnotatedString {
                append("📧 ")
                pushStringAnnotation(tag = "EMAIL", annotation = "mailto:freegameradar.app@gmail.com")
                withStyle(style = SpanStyle(color = Color(0xFF10B981), textDecoration = TextDecoration.Underline)) {
                    append("freegameradar.app@gmail.com")
                }
                pop()
            }
            ClickableText(
                text = annotatedEmailText,
                style = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color(0xFF9CA3AF)),
                onClick = { offset ->
                    annotatedEmailText.getStringAnnotations(tag = "EMAIL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
        }
    }
}

@Composable
fun TermsOfServiceSection() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Terms of Service",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("By using FreeGameRadar, you agree to the following terms:", fontSize = 14.sp, color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))

            Text("General Use", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• The app is provided \"as is\" without warranties of any kind", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• FreeGameRadar does not guarantee the accuracy, availability, or duration of any free game offers", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("Third-Party Content", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            val annotatedServiceText = buildAnnotatedString {
                append("• Game data is sourced from third-party services such as the GamerPower API (")
                pushStringAnnotation(tag = "URL", annotation = "https://www.gamerpower.com")
                withStyle(style = SpanStyle(color = Color(0xFF10B981), textDecoration = TextDecoration.Underline)) {
                    append("https://www.gamerpower.com")
                }
                pop()
                append(")")
            }
            ClickableText(
                text = annotatedServiceText,
                style = LocalTextStyle.current.copy(fontSize = 13.sp, color = Color(0xFF9CA3AF)),
                onClick = { offset ->
                    annotatedServiceText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("• All game titles, images, and trademarks belong to their respective owners", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• FreeGameRadar is not affiliated with or endorsed by Epic Games, Steam, GOG, or any publisher", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("Accounts", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• Account creation is optional", fontSize = 13.sp, color = Color(0xFF9CA3AF))
            Text("• Users are responsible for maintaining the security of their account credentials", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("Acceptable Use", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• Do not misuse the app or attempt unauthorized access", fontSize = 13.sp, color = Color(0xFF9CA3AF))

            Spacer(modifier = Modifier.height(12.dp))
            Text("Changes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF34D399))
            Spacer(modifier = Modifier.height(6.dp))
            Text("• These terms may be updated to reflect improvements or legal requirements", fontSize = 13.sp, color = Color(0xFF9CA3AF))
        }
    }
}

@Composable
fun ContactSection() {
    val uriHandler = LocalUriHandler.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B263B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Contact & Feedback",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("We'd love to hear from you!", fontSize = 14.sp, color = Color(0xFFE5E7EB))
            Spacer(modifier = Modifier.height(12.dp))
            val annotatedEmailText = buildAnnotatedString {
                append("📧 Email: ")
                pushStringAnnotation(tag = "EMAIL", annotation = "mailto:freegameradar.app@gmail.com")
                withStyle(style = SpanStyle(color = Color(0xFF10B981), textDecoration = TextDecoration.Underline)) {
                    append("freegameradar.app@gmail.com")
                }
                pop()
            }
            ClickableText(
                text = annotatedEmailText,
                style = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color(0xFFE5E7EB)),
                onClick = { offset ->
                    annotatedEmailText.getStringAnnotations(tag = "EMAIL", start = offset, end = offset)
                        .firstOrNull()?.let { annotation ->
                            uriHandler.openUri(annotation.item)
                        }
                }
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text("📝 Feedback: Bug reports, feature requests, and suggestions are welcome", fontSize = 14.sp, color = Color(0xFFE5E7EB))
        }
    }
}

@Composable
fun AppInfoFooter() {
    val uriHandler = LocalUriHandler.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("© 2026 Kartik Patel", fontSize = 12.sp, color = Color(0xFF6B7280))
        Text("Made with ❤️ using Kotlin Multiplatform", fontSize = 12.sp, color = Color(0xFF6B7280))
        Spacer(modifier = Modifier.height(8.dp))
        Text("Game giveaway data powered by GamerPower API", fontSize = 12.sp, color = Color(0xFF6B7280))
        val annotatedLinkText = buildAnnotatedString {
            pushStringAnnotation(tag = "URL", annotation = "https://www.gamerpower.com")
            withStyle(style = SpanStyle(color = Color(0xFF10B981), textDecoration = TextDecoration.Underline)) {
                append("https://www.gamerpower.com")
            }
            pop()
        }
        ClickableText(
            text = annotatedLinkText,
            style = LocalTextStyle.current.copy(fontSize = 12.sp),
            onClick = { offset ->
                annotatedLinkText.getStringAnnotations(tag = "URL", start = offset, end = offset)
                    .firstOrNull()?.let { annotation ->
                        uriHandler.openUri(annotation.item)
                    }
            }
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "FreeGameRadar is an independent project and is not affiliated with or endorsed by Epic Games, Steam, GOG, or any game publisher.",
            fontSize = 10.sp,
            color = Color(0xFF6B7280),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}