package com.example.freegameradar.ui.screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.compose.ui.Modifier

@Composable
fun Settings(navController: NavController, modifier: Modifier)
{
    Box(modifier = modifier.fillMaxSize()) {
        Text(text = "Settings Section")
    }
}
