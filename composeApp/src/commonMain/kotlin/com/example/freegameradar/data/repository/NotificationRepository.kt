package com.example.freegameradar.data.repository

import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.db.GameDatabase

class NotificationRepository(private val database: GameDatabase) {

    private val queries = database.notificationsQueries

    fun saveNotification(notification: DealNotification) {
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

    fun getAllNotifications(): List<DealNotification> {
        return queries.getAllNotifications().executeAsList().map {
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
    }

    fun markAsRead(id: Long) {
        queries.markAsRead(id)
    }

    fun getUnreadCount(): Int {
        return queries.getUnreadNotificationCount().executeAsOne().toInt()
    }

    fun deleteNotification(id: Long) {
        queries.deleteNotificationById(id)
    }

    fun deleteExpiredNotifications(validGameIds: List<Long>) {
        queries.deleteExpiredNotifications(validGameIds)
    }
}
