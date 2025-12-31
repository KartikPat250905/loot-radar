package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.data.repository.NotificationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    val notifications: StateFlow<List<DealNotification>> = notificationRepository.getAllNotifications()
        .map { list -> 
            println("NotificationViewModel: Loaded ${list.size} notifications from repository.")
            list.sortedByDescending { it.timestamp } 
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val unreadNotificationCount: StateFlow<Int> = notificationRepository.getUnreadCount()
        .map { it.toInt() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            println("NotificationViewModel: Marked all notifications as read.")
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
        }
    }

    // Added this function to fix the build error.
    // Since the UI is reactive, no action is needed here to refresh from the local DB.
    // This can be used later for server-side fetching.
    fun refreshNotifications() {
        // Currently empty as the notifications Flow is always up-to-date.
        println("NotificationViewModel: Refresh requested (currently a no-op).")
    }
}
