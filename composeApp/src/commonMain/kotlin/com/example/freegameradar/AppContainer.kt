package com.example.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.image.AppImageLoader
import coil3.annotation.ExperimentalCoilApi
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.db.GameDatabase

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable (GameDatabase) -> Unit) {
    val driver = remember { DatabaseDriverFactory.createDriver() }
    val database = remember { GameDatabase(driver) }

    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get()
    }
    content(database)
}
