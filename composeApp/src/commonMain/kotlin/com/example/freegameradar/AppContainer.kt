package com.example.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.image.AppImageLoader
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.data.repository.NotificationRepository
import com.example.freegameradar.db.GameDatabase

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable (notificationRepository: NotificationRepository) -> Unit) {
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get(context)
    }

    // Create a single instance of the database and repository for the UI
    val driver = remember { DatabaseDriverFactory.createDriver() }
    val database = remember { GameDatabase(driver) }
    val notificationRepository = remember { NotificationRepository(database) }

    content(notificationRepository)
}
