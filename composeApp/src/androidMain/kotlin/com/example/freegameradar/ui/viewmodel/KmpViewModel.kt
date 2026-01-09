package com.example.freegameradar.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewModelScope as androidViewModelScope
import kotlinx.coroutines.CoroutineScope

// ✅ Android: actual class extends ViewModel
actual abstract class KmpViewModel : ViewModel() {
    actual val viewModelScope: CoroutineScope
        get() = androidViewModelScope
}

@Composable
actual fun <T : KmpViewModel> rememberKmpViewModel(key: Any?, factory: () -> T): T {
    val viewModel = viewModel<ViewModel>(
        key = key?.toString(),
        factory = object : ViewModelProvider.Factory {
            override fun <VM : ViewModel> create(modelClass: Class<VM>): VM {
                @Suppress("UNCHECKED_CAST")
                return factory() as VM
            }
        }
    )
    @Suppress("UNCHECKED_CAST")
    return viewModel as T
}
