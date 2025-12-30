package com.example.freegameradar.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.data.repository.NotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    private val repository: NotificationRepository
) : ViewModel() {

    private val _notifications = MutableStateFlow<List<DealNotification>>(emptyList())
    val notifications: StateFlow<List<DealNotification>> = _notifications.asStateFlow()

    init {
        println("NotificationViewModel: ViewModel initialized, loading notifications...")
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            try {
                val allNotifications = repository.getAllNotifications()
                println("NotificationViewModel: Loaded ${allNotifications.size} notifications from repository")
                _notifications.value = allNotifications.sortedByDescending { it.timestamp }
            } catch (e: Exception) {
                println("NotificationViewModel: Error loading notifications - ${e.message}")
                e.printStackTrace()
                _notifications.value = emptyList()
            }
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                repository.markAllAsRead()
                println("NotificationViewModel: Marked all notifications as read")
                loadNotifications() // Reload to update UI
            } catch (e: Exception) {
                println("NotificationViewModel: Error marking all as read - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun deleteNotification(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteNotification(id)
                println("NotificationViewModel: Deleted notification with id: $id")
                loadNotifications() // Reload to update UI
            } catch (e: Exception) {
                println("NotificationViewModel: Error deleting notification - ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun refreshNotifications() {
        println("NotificationViewModel: Manually refreshing notifications...")
        loadNotifications()
    }
}