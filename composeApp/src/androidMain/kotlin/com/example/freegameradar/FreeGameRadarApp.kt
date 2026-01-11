package com.example.freegameradar

import android.app.Application
import android.util.Log
import androidx.core.app.NotificationManagerCompat
import com.example.freegameradar.data.DatabaseDriverFactory
import com.example.freegameradar.data.GameDatabaseProvider
import com.example.freegameradar.data.repository.NotificationRepository
import com.example.freegameradar.notification.NotificationService
import com.example.freegameradar.notification.TokenManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

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

        val databaseDriverFactory = DatabaseDriverFactory(this)
        GameDatabaseProvider.init(databaseDriverFactory.createDriver())
        val database = GameDatabaseProvider.getDatabase()

        notificationRepository = NotificationRepository(database)

        val notificationService = NotificationService(this)
        notificationService.createNotificationChannel()

        NotificationManagerCompat.from(this).cancelAll()

        if (Firebase.auth.currentUser != null) {
            Log.d("App", "User already logged in: ${Firebase.auth.currentUser?.uid}")
            TokenManager.initializeFCMToken()
        }

        Firebase.auth.addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                Log.d("App", "Auth state changed, user logged in")
                TokenManager.initializeFCMToken()
            }
        }
    }
}
