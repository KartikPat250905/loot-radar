package com.example.freegameradar

import androidx.compose.ui.window.ComposeUIViewController
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.ui.viewmodel.AuthViewModel

fun MainViewController() = ComposeUIViewController { App(AuthViewModel(AuthRepositoryImpl())) }
