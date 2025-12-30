package com.example.freegameradar.data.repository

import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.db.GameDatabase

class NotificationRepository(private val database: GameDatabase) {

    private val queries = database.notificationsQueries

    fun saveNotification(notification: DealNotification) {
        try {
            queries.insertNotification(
                id = notification.id,
                title = notification.title,
                description = notification.description,
                url = notification.url,
                imageUrl = notification.imageUrl,
                timestamp = notification.timestamp,
                isRead = if (notification.isRead) 1L else 0L
            )
            println("NotificationRepository: Saved notification ${notification.id} - ${notification.title}")
        } catch (e: Exception) {
            println("NotificationRepository: Error saving notification ${notification.id}")
            e.printStackTrace()
        }
    }

    fun saveNotifications(notifications: List<DealNotification>) {
        try {
            queries.transaction {
                notifications.forEach { notification ->
                    queries.insertNotification(
                        id = notification.id,
                        title = notification.title,
                        description = notification.description,
                        url = notification.url,
                        imageUrl = notification.imageUrl,
                        timestamp = notification.timestamp,
                        isRead = if (notification.isRead) 1L else 0L
                    )
                }
            }
            println("NotificationRepository: Saved ${notifications.size} notifications in batch")
        } catch (e: Exception) {
            println("NotificationRepository: Error saving batch notifications")
            e.printStackTrace()
        }
    }

    fun getAllNotifications(): List<DealNotification> {
        return try {
            val result = queries.getAllNotifications().executeAsList().map {
                DealNotification(
                    id = it.id,
                    title = it.title,
                    description = it.description,
                    url = it.url,
                    imageUrl = it.imageUrl,
                    timestamp = it.timestamp,
                    isRead = it.isRead == 1L
                )
            }
            println("NotificationRepository: Retrieved ${result.size} notifications")
            result
        } catch (e: Exception) {
            println("NotificationRepository: Error getting all notifications")
            e.printStackTrace()
            emptyList()
        }
    }

    fun markAsRead(id: Long) {
        try {
            queries.markAsRead(id)
            println("NotificationRepository: Marked notification $id as read")
        } catch (e: Exception) {
            println("NotificationRepository: Error marking notification $id as read")
            e.printStackTrace()
        }
    }

    fun markAllAsRead() {
        try {
            queries.markAllAsRead()
            println("NotificationRepository: Marked all notifications as read")
        } catch (e: Exception) {
            println("NotificationRepository: Error marking all as read")
            e.printStackTrace()
        }
    }

    fun getUnreadCount(): Int {
        return try {
            val count = queries.getUnreadNotificationCount().executeAsOne().toInt()
            println("NotificationRepository: Unread count = $count")
            count
        } catch (e: Exception) {
            println("NotificationRepository: Error getting unread count")
            e.printStackTrace()
            0
        }
    }

    fun deleteNotification(id: Long) {
        try {
            queries.deleteNotificationById(id)
            println("NotificationRepository: Deleted notification $id")
        } catch (e: Exception) {
            println("NotificationRepository: Error deleting notification $id")
            e.printStackTrace()
        }
    }

    fun deleteExpiredNotifications(validGameIds: List<Long>) {
        try {
            queries.deleteExpiredNotifications(validGameIds)
            println("NotificationRepository: Cleaned up expired notifications")
        } catch (e: Exception) {
            println("NotificationRepository: Error deleting expired notifications")
            e.printStackTrace()
        }
    }
}