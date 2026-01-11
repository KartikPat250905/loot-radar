package com.example.freegameradar.ui.viewmodel

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope

expect abstract class KmpViewModel() {
    val viewModelScope: CoroutineScope
}

@Composable
expect fun <T : KmpViewModel> rememberKmpViewModel(
    key: Any? = null,
    factory: () -> T
): T
