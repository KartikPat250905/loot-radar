package com.example.freegameradar.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.db.GameDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NotificationRepository(private val database: GameDatabase) {

    private val queries = database.notificationsQueries

    fun saveNotifications(notifications: List<DealNotification>) {
        queries.transaction {
            notifications.forEach { notification ->
                queries.insertNotification(
                    id = notification.id,
                    title = notification.title,
                    worth = notification.worth,
                    description = notification.description,
                    url = notification.url,
                    imageUrl = notification.imageUrl,
                    timestamp = notification.timestamp,
                    isRead = if (notification.isRead) 1L else 0L
                )
            }
        }
    }

    fun getAllNotifications(): Flow<List<DealNotification>> {
        return queries.getAllNotifications()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { entity ->
                    DealNotification(
                        id = entity.id,
                        title = entity.title,
                        worth = entity.worth,
                        description = entity.description,
                        url = entity.url,
                        imageUrl = entity.imageUrl,
                        timestamp = entity.timestamp,
                        isRead = entity.isRead == 1L
                    )
                }
            }
    }

    fun markAsRead(id: Long) {
        queries.markAsRead(id)
    }

    fun markAllAsRead() {
        queries.markAllAsRead()
    }

    fun getUnreadCount(): Flow<Long> {
        return queries.getUnreadNotificationCount().asFlow().mapToOne(Dispatchers.IO)
    }

    fun deleteAllNotifications() {
        queries.clearAllNotifications()
    }

    fun deleteNotification(id: Long) {
        queries.deleteNotificationById(id)
    }

    fun deleteExpiredNotifications(validGameIds: List<Long>) {
        queries.deleteExpiredNotifications(validGameIds)
    }
}
