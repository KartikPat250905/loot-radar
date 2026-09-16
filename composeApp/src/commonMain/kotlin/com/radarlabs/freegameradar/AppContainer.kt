package com.radarlabs.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.radarlabs.freegameradar.core.createSettings
import com.radarlabs.freegameradar.core.image.AppImageLoader
import com.radarlabs.freegameradar.data.DatabaseDriverFactory
import com.radarlabs.freegameradar.data.auth.AuthRepositoryImpl
import com.radarlabs.freegameradar.data.remote.ApiService
import com.radarlabs.freegameradar.data.repository.GameRepository
import com.radarlabs.freegameradar.data.repository.NotificationRepository
import com.radarlabs.freegameradar.data.repository.UserStatsRepository
import com.radarlabs.freegameradar.db.GameDatabase

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable (gameRepository: GameRepository, notificationRepository: NotificationRepository, userStatsRepository: UserStatsRepository) -> Unit) {
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get(context)
    }

    // Create a single instance of the database and repository for the UI
    val driver = remember { DatabaseDriverFactory.createDriver() }
    val database = remember { GameDatabase(driver) }
    val apiService = remember { ApiService() }
    val gameRepository = remember { GameRepository(apiService) }
    val notificationRepository = remember { NotificationRepository(database) }
    val authRepository = remember { AuthRepositoryImpl() }
    val settings = remember { createSettings() } // Use the new factory
    val userStatsRepository = remember { UserStatsRepository(authRepository, settings) }

    content(gameRepository, notificationRepository, userStatsRepository)
}
