package com.radarlabs.freegameradar

import android.app.Application
import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.radarlabs.freegameradar.data.DatabaseDriverFactory
import com.radarlabs.freegameradar.data.repository.NotificationRepository
import com.radarlabs.freegameradar.db.GameDatabase
import com.radarlabs.freegameradar.notification.NotificationService
import com.radarlabs.freegameradar.notification.TokenManager

class FreeGameRadarApp : Application() {

    lateinit var notificationRepository: NotificationRepository
        private set

    companion object {
        lateinit var instance: FreeGameRadarApp
            private set
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        appContext = applicationContext

        // Initialize the database driver and create a single repository instance
        DatabaseDriverFactory.init(this)
        val database = GameDatabase(DatabaseDriverFactory.createDriver())
        notificationRepository = NotificationRepository(database)

        // Create the notification channel as soon as the app starts
        val notificationService = NotificationService(this)
        notificationService.createNotificationChannel()

        // Per your instruction, clear all previously stuck notifications on startup.
        NotificationManagerCompat.from(this).cancelAll()

        // Initialize FCM token manager
        TokenManager.initializeFCMToken()
    }
}
