// composeApp/src/androidMain/kotlin/com/example/lootradar/MainActivity.kt
package com.example.lootradarkmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.lootradarkmp.core.image.AndroidContextHolder
import com.example.lootradarkmp.data.auth.AuthRepositoryImpl
import com.example.lootradarkmp.ui.viewmodel.AuthViewModel

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