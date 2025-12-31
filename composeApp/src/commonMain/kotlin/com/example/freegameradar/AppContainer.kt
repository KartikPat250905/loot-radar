package com.example.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.createSettings
import com.example.freegameradar.core.image.AppImageLoader
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.data.repository.NotificationRepository
import com.example.freegameradar.data.repository.UserStatsRepository
import com.example.freegameradar.db.GameDatabase

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable (notificationRepository: NotificationRepository, userStatsRepository: UserStatsRepository) -> Unit) {
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get(context)
    }

    // Create a single instance of the database and repository for the UI
    val driver = remember { DatabaseDriverFactory.createDriver() }
    val database = remember { GameDatabase(driver) }
    val notificationRepository = remember { NotificationRepository(database) }
    val authRepository = remember { AuthRepositoryImpl() }
    val settings = remember { createSettings() } // Use the new factory
    val userStatsRepository = remember { UserStatsRepository(authRepository, settings) }

    content(notificationRepository, userStatsRepository)
}
