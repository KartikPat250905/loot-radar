package com.example.freegameradar

import androidx.compose.runtime.Composable
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.image.AppImageLoader
import coil3.annotation.ExperimentalCoilApi

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable () -> Unit) {
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get()
    }
    content()
}