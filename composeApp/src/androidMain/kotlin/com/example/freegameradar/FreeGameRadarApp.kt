package com.example.freegameradar

import android.app.Application
import androidx.core.app.NotificationManagerCompat
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.data.repository.NotificationRepository
import com.example.freegameradar.db.GameDatabase
import com.example.freegameradar.notification.NotificationService

class FreeGameRadarApp : Application() {

    lateinit var notificationRepository: NotificationRepository
        private set

    companion object {
        lateinit var instance: FreeGameRadarApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize the database driver and create a single repository instance
        DatabaseDriverFactory.init(this)
        val database = GameDatabase(DatabaseDriverFactory.createDriver())
        notificationRepository = NotificationRepository(database)

        // Create the notification channel as soon as the app starts
        val notificationService = NotificationService(this)
        notificationService.createNotificationChannel()

        // Per your instruction, clear all previously stuck notifications on startup.
        NotificationManagerCompat.from(this).cancelAll()
    }
}
