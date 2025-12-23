// composeApp/src/androidMain/kotlin/com/example/lootradar/MainActivity.kt
package com.example.freegameradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.freegameradar.core.image.AndroidContextHolder
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidContextHolder.context = applicationContext
        enableEdgeToEdge()
        val authRepository = AuthRepositoryImpl()
        val authViewModel = AuthViewModel(authRepository)

        authViewModel.checkAuthState()
        setContent {
            App(authViewModel = authViewModel)
        }
    }
}