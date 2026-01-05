package com.example.freegameradar.ui.viewmodel

import androidx.compose.runtime.Composable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

open class KmpViewModel {
    protected val viewModelScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    open fun onCleared() {
        viewModelScope.cancel()
    }
}

@Composable
expect fun <T : KmpViewModel> rememberKmpViewModel(key: Any? = null, factory: () -> T): T
