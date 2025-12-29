package com.example.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.image.AppImageLoader
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.db.GameDatabase

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable (database: GameDatabase) -> Unit) {
    // Configure the image loader, passing the context
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get(context)
    }

    // Create and remember the database instance
    val driver = remember { DatabaseDriverFactory.createDriver() }
    val database = remember { GameDatabase(driver) }

    // Provide the database to the content
    content(database)
}
