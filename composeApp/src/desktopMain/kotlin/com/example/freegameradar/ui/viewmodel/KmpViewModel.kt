package com.example.freegameradar.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

// ✅ Desktop: actual class creates its own scope
actual abstract class KmpViewModel {
    actual val viewModelScope: CoroutineScope = 
        CoroutineScope(SupervisorJob() + Dispatchers.Main)
}

@Composable
actual fun <T : KmpViewModel> rememberKmpViewModel(key: Any?, factory: () -> T): T {
    return remember(key) { factory() }
}