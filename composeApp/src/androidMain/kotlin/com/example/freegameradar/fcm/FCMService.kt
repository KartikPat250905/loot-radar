package com.example.freegameradar.fcm

import com.example.freegameradar.notification.NotificationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val notificationService = NotificationService(applicationContext)
        notificationService.createNotificationChannel() // Ensure channel is created

        // Extract deal count from the notification, with a fallback to 1
        val dealCount = remoteMessage.data["dealCount"]?.toIntOrNull() ?: 1
        
        // Show the summary notification
        notificationService.showNewDealsNotification(dealCount)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Optional: Send the new token to your server to keep it up-to-date
        // This is useful for ensuring that notifications are always sent to the correct device
    }
}
