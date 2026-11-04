package com.radarlabs.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.radarlabs.freegameradar.data.model.DealNotification
import com.radarlabs.freegameradar.data.repository.GameRepository
import com.radarlabs.freegameradar.data.repository.NotificationRepository
import com.radarlabs.freegameradar.ui.screens.isExpired
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar

class NotificationViewModel(
    private val notificationRepository: NotificationRepository,
    private val gameRepository: GameRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val notifications: StateFlow<List<DealNotification>> = notificationRepository.getAllNotifications()
        .map { notifications ->
            notifications.sortedByDescending { notification ->
                notification.worth.removePrefix("$").toFloatOrNull() ?: 0.0f
            }
        }
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val endedNotificationIds: StateFlow<Set<Long>> = combine(
        notifications,
        gameRepository.getFreeGames().catch { emit(emptyList()) }
    ) { notificationsList, gamesList ->
        if (gamesList.isEmpty()) {
            emptySet()
        } else {
            val gameMap = gamesList.associateBy { it.id }
            notificationsList.filter { notification ->
                val game = gameMap[notification.id]
                game == null ||
                        game.status?.equals("Ended", ignoreCase = true) == true ||
                        game.status?.equals("Expired", ignoreCase = true) == true ||
                        game.status?.equals("Closed", ignoreCase = true) == true ||
                        isExpired(game.end_date)
            }.map { it.id }.toSet()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptySet()
    )

    val unreadNotificationCount: StateFlow<Int> = notificationRepository.getUnreadCount()
        .map { it.toInt() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(id: Long) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    fun clearAllnotifications() {
        viewModelScope.launch {
            notificationRepository.deleteAllNotifications()
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun refreshNotifications() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                gameRepository.getFreeGamesState(forceRefresh = true).collect { }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _error.value = "Couldn't refresh. Check your connection."
                println("Error refreshing notifications: ${e.message}")
            } finally {
                _isRefreshing.value = false
            }
        }
    }
}
