package com.example.freegameradar

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.remote.ApiService
import com.example.freegameradar.data.repository.GameRepository
import com.example.freegameradar.data.repository.NotificationRepository

@Composable
fun AppContainer(content: @Composable (gameRepository: GameRepository, notificationRepository: NotificationRepository) -> Unit) {

    val database = remember { GameDatabaseProvider.getDatabase() }
    val apiService = remember { ApiService() }
    val gameRepository = remember { GameRepository(apiService) }
    val notificationRepository = remember { NotificationRepository(database) }

    content(gameRepository, notificationRepository)
}
