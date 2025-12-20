package com.example.lootradarkmp

import androidx.compose.runtime.Composable
import coil3.compose.setSingletonImageLoaderFactory
import com.example.lootradarkmp.core.image.AppImageLoader
import androidx.compose.runtime.CompositionLocalProvider
import coil3.annotation.ExperimentalCoilApi

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable () -> Unit) {
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get()
    }
    content()
}