package com.example.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.setSingletonImageLoaderFactory
import com.example.freegameradar.core.createSettings
import com.example.freegameradar.core.image.AppImageLoader
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.auth.AuthRepositoryImpl
import com.example.freegameradar.data.remote.ApiService
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.repository.NotificationRepository
import com.example.freegameradar.data.repository.UserStatsRepository

@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppContainer(content: @Composable (gameRepository: GameRepository, notificationRepository: NotificationRepository, userStatsRepository: UserStatsRepository) -> Unit) {
    setSingletonImageLoaderFactory { context ->
        AppImageLoader.get(context)
    }

    // The database is now initialized synchronously in main.kt
    val database = remember { GameDatabaseProvider.getDatabase() }
    
    val apiService = remember { ApiService() }
    val gameRepository = remember { GameRepository(apiService) }
    val notificationRepository = remember { NotificationRepository(database) }
    val authRepository = remember { AuthRepositoryImpl() }
    val settings = remember { createSettings() } // Use the new factory
    val userStatsRepository = remember { UserStatsRepository(authRepository, settings) }

    content(gameRepository, notificationRepository, userStatsRepository)
}
