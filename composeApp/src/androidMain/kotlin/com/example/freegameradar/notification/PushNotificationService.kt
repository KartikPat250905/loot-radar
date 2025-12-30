package com.example.freegameradar.notification

import com.example.freegameradar.FreeGameRadarApp
import com.example.freegameradar.data.model.DealNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.serialization.json.Json
import com.example.freegameradar.notification.NotificationHelper

class PushNotificationService : FirebaseMessagingService() {

    private val repository by lazy { FreeGameRadarApp.instance.notificationRepository }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        println("PushNotificationService: === MESSAGE RECEIVED ===")
        println("PushNotificationService: From: ${message.from}")
        println("PushNotificationService: Data: ${message.data}")

        message.data["deals"]?.let { dealsJson ->
            println("PushNotificationService: Raw deals JSON length: ${dealsJson.length}")
            println("PushNotificationService: First 200 chars: ${dealsJson.take(200)}")

            try {
                val deals = Json.decodeFromString<List<DealNotification>>(dealsJson)
                repository.saveNotifications(deals)

                println("PushNotificationService: About to call getAllNotifications to verify...")
                val allSaved = repository.getAllNotifications()
                println("PushNotificationService: Database now has ${allSaved.size} total notifications")

                // Optionally, show a system notification
                if (deals.isNotEmpty()) {
                    NotificationHelper.showNotification(
                        context = this,
                        title = "New Game Deals!",
                        content = "${deals.size} new deals match your watchlist."
                    )
                }
            } catch (e: Exception) {
                println("PushNotificationService: Error processing deals from push: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        println("PushNotificationService: New FCM Token: $token")
        // Here you would typically send the new token to your server
    }
}
