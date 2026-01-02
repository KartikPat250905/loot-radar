package com.example.freegameradar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            AboutSection()
            Spacer(modifier = Modifier.height(16.dp))
            AppVersionSection()
            Spacer(modifier = Modifier.height(16.dp))
            PrivacyPolicySection()
            Spacer(modifier = Modifier.height(16.dp))
            TermsOfServiceSection()
            Spacer(modifier = Modifier.height(16.dp))
            ContactSection()
            Spacer(modifier = Modifier.height(24.dp))
            AppInfoFooter()
        }
    }
}

@Composable
fun AboutSection() {
    Column {
        Text("About FreeGameRadar", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "FreeGameRadar is a cross-platform application built with Kotlin Multiplatform that helps gamers discover legitimate free game offers from popular platforms such as Epic Games Store, Steam, GOG, and more.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "The app aggregates publicly available giveaway data from trusted third-party sources, including the GamerPower API (https://www.gamerpower.com), and notifies users when new free games become available — so you never miss a deal.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "FreeGameRadar supports Guest Mode (no account required) as well as an optional account system for users who want personalized notifications and preferences.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun AppVersionSection() {
    Column {
        Text("App Version", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("v1.0.0", style = MaterialTheme.typography.bodyMedium)
        Text("(Kotlin Multiplatform)", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
fun PrivacyPolicySection() {
    Column {
        Text("Privacy Policy", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("FreeGameRadar respects your privacy.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("What we collect", style = MaterialTheme.typography.titleSmall)
        Text("• No personal data is required in Guest Mode", style = MaterialTheme.typography.bodyMedium)
        Text("• If you create an account, we may collect:", style = MaterialTheme.typography.bodyMedium)
        Text("  - Email address (for authentication and notifications)", style = MaterialTheme.typography.bodyMedium)
        Text("  - App preferences (notification settings, platform filters)", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("What we do NOT collect", style = MaterialTheme.typography.titleSmall)
        Text("• No payment information", style = MaterialTheme.typography.bodyMedium)
        Text("• No sensitive personal data", style = MaterialTheme.typography.bodyMedium)
        Text("• No cross-app or cross-website tracking", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Third-Party Services", style = MaterialTheme.typography.titleSmall)
        Text(
            "• Game giveaway data is provided by the GamerPower API (https://www.gamerpower.com)",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            "• FreeGameRadar only displays publicly available information and does not own or control third-party content",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text("Data Usage", style = MaterialTheme.typography.titleSmall)
        Text("• Data is used solely to provide app functionality", style = MaterialTheme.typography.bodyMedium)
        Text("• Data is never sold or shared for advertising purposes", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Contact", style = MaterialTheme.typography.titleSmall)
        Text("For privacy-related questions, contact:", style = MaterialTheme.typography.bodyMedium)
        Text("📧 freegameradar.app@gmail.com", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun TermsOfServiceSection() {
    Column {
        Text("Terms of Service", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Text("By using FreeGameRadar, you agree to the following terms:", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("General Use", style = MaterialTheme.typography.titleSmall)
        Text("• The app is provided “as is” without warranties of any kind", style = MaterialTheme.typography.bodyMedium)
        Text("• FreeGameRadar does not guarantee the accuracy, availability, or duration of any free game offers", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Third-Party Content", style = MaterialTheme.typography.titleSmall)
        Text(
            "• Game data is sourced from third-party services such as the GamerPower API (https://www.gamerpower.com)",
            style = MaterialTheme.typography.bodyMedium
        )
        Text("• All game titles, images, and trademarks belong to their respective owners", style = MaterialTheme.typography.bodyMedium)
        Text("• FreeGameRadar is not affiliated with or endorsed by Epic Games, Steam, GOG, or any publisher", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Accounts", style = MaterialTheme.typography.titleSmall)
        Text("• Account creation is optional", style = MaterialTheme.typography.bodyMedium)
        Text("• Users are responsible for maintaining the security of their account credentials", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Acceptable Use", style = MaterialTheme.typography.titleSmall)
        Text("• Do not misuse the app or attempt unauthorized access", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(8.dp))
        Text("Changes", style = MaterialTheme.typography.titleSmall)
        Text("• These terms may be updated to reflect improvements or legal requirements", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun ContactSection() {
    Column {
        Text("Contact & Feedback", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("We’d love to hear from you!", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("📧 Email: freegameradar.app@gmail.com", style = MaterialTheme.typography.bodyMedium)
        Text("📝 Feedback: Bug reports, feature requests, and suggestions are welcome", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AppInfoFooter() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("© 2026 Kartik Patel", fontSize = 12.sp)
        Text("Made with ❤️ using Kotlin Multiplatform", fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Game giveaway data powered by GamerPower API", fontSize = 12.sp)
        Text("https://www.gamerpower.com", fontSize = 12.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "FreeGameRadar is an independent project and is not affiliated with or endorsed by Epic Games, Steam, GOG, or any game publisher.",
            fontSize = 10.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

