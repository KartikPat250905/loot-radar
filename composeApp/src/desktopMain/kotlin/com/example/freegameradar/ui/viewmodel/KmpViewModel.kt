package com.example.freegameradar.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember

@Composable
actual fun <T : KmpViewModel> rememberKmpViewModel(key: Any?, factory: () -> T): T {
    val viewModel = remember(key) { factory() }
    DisposableEffect(viewModel) {
        onDispose {
            viewModel.onCleared()
        }
    }
    return viewModel
}
