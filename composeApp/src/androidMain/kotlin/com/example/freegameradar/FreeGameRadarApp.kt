package com.example.freegameradar

import android.app.Application
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.data.repository.NotificationRepository
import com.example.freegameradar.db.GameDatabase

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
    }
}
