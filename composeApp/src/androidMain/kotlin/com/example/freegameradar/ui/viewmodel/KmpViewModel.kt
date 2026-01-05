package com.example.freegameradar.ui.viewmodel

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
actual fun <T : KmpViewModel> rememberKmpViewModel(key: Any?, factory: () -> T): T {
    return viewModel(modelClass = factory.toModelClass(), factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return factory() as T
        }
    })
}

private fun <T> (() -> T).toModelClass(): Class<T> {
    return javaClass.declaredFields
        .firstOrNull { it.name == "INSTANCE" }?.get(null)?.javaClass as? Class<T>
        ?: error("Could not find model class for factory $this")
}
