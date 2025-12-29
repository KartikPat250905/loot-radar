package com.example.freegameradar.notification

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PushNotificationService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send this token to your server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val notificationService = NotificationService(applicationContext)
        notificationService.createNotificationChannel() // Ensure channel is created

        // Extract deal count from the notification, with a fallback to 1
        val dealCount = message.data["dealCount"]?.toIntOrNull() ?: 1
        
        // Show the summary notification
        notificationService.showNewDealsNotification(dealCount)
    }
}
