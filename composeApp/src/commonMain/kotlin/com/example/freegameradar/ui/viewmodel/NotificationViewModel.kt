package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(private val notificationRepository: NotificationRepository) : ViewModel() {

    private val _notifications = MutableStateFlow<List<DealNotification>>(emptyList())
    val notifications: StateFlow<List<DealNotification>> = _notifications.asStateFlow()

    init {
        // The init block already calls loadNotifications, as requested.
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val notificationsFromRepo = notificationRepository.getAllNotifications()
            // Added logging to check the data from the repository.
            println("NotificationViewModel: Loaded ${notificationsFromRepo.size} notifications from repository.")
            // Sort notifications by timestamp, newest first, as requested.
            _notifications.value = notificationsFromRepo.sortedByDescending { it.timestamp }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
            loadNotifications() // Refresh the list to show them as read.
            println("NotificationViewModel: Marked all notifications as read.")
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(id)
            loadNotifications() // Refresh the list
        }
    }
}
