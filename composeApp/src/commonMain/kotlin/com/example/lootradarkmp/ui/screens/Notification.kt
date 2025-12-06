package com.example.lootradarkmp.ui.screens
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun Notification(navController: NavController, modifier: Modifier)
{
    Box(modifier = modifier.fillMaxSize()) {
        Text(text = "Notification Section")
    }
}
