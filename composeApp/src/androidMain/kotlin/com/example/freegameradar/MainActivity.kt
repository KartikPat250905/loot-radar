package com.example.freegameradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.ui.theme.ModernDarkTheme
import com.example.freegameradar.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val startRoute = intent.getStringExtra("route")

        setContent {
            ModernDarkTheme {
                App(
                    authViewModel = AuthViewModel(AuthRepositoryImpl()),
                    startRoute = startRoute
                )
            }
        }
    }
}
