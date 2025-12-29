package com.example.freegameradar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.ui.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(AuthViewModel(AuthRepositoryImpl()))
        }
    }
}
