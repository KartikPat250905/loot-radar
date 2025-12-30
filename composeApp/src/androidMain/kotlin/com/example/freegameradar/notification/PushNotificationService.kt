package com.example.freegameradar.notification

import android.util.Log
import com.example.freegameradar.FreeGameRadarApp
import com.example.freegameradar.data.model.DealNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// A data class to match the structure of deals sent in the FCM message
@Serializable
data class FcmDeal(
    val id: Long,
    val title: String,
    val description: String,
    val open_giveaway_url: String,
    val image: String
)

@Serializable
data class DealsWrapper(val deals: List<FcmDeal>)

class PushNotificationService : FirebaseMessagingService() {

    // A lazy-initialized JSON parser
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Send this token to your server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d("PushNotificationService", "From: ${message.from}")

        // This block will be executed if the message has a data payload.
        if (message.data.isNotEmpty()) {
            Log.d("PushNotificationService", "Message data payload: " + message.data)
        }

        // The server sends a custom 'data' message. We handle it here.
        message.data["deals"]?.let { dealsJson ->
            Log.d("PushNotificationService", "Received deals JSON: $dealsJson")

            val fcmDeals = try {
                // Case 1: The JSON string is a direct array of deals.
                json.decodeFromString<List<FcmDeal>>(dealsJson)
            } catch (e1: Exception) {
                Log.d("PushNotificationService", "Attempt 1: Failed to parse as List<FcmDeal>. Trying next case.")
                try {
                    // Case 2: The JSON string is an object containing a 'deals' array.
                    json.decodeFromString<DealsWrapper>(dealsJson).deals
                } catch (e2: Exception) {
                    Log.d("PushNotificationService", "Attempt 2: Failed to parse as DealsWrapper. Trying next case.")
                    try {
                        // Case 3: The JSON string is a single deal object.
                        listOf(json.decodeFromString<FcmDeal>(dealsJson))
                    } catch (e3: Exception) {
                        // If all parsing attempts fail, log the error and exit.
                        Log.e("PushNotificationService", "All parsing attempts failed.", e3)
                        return
                    }
                }
            }

            if (fcmDeals.isNotEmpty()) {
                Log.d("PushNotificationService", "Parsed ${fcmDeals.size} deals successfully.")
                // Get the singleton repository instance from our Application class
                val repository = FreeGameRadarApp.instance.notificationRepository

                // Map the incoming deals to our local DealNotification model
                val notificationsToSave = fcmDeals.map { fcmDeal ->
                    DealNotification(
                        id = fcmDeal.id,
                        title = fcmDeal.title,
                        description = fcmDeal.description,
                        url = fcmDeal.open_giveaway_url,
                        imageUrl = fcmDeal.image,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                }

                // Save all notifications in a single transaction
                repository.saveNotifications(notificationsToSave)
                Log.d("PushNotificationService", "Saved ${notificationsToSave.size} notifications.")


                // After saving, show a single, themed summary notification
                val notificationService = NotificationService(applicationContext)
                notificationService.createNotificationChannel() // Ensure channel exists
                notificationService.showNewDealsNotification(notificationsToSave.size)
                Log.d("PushNotificationService", "showNewDealsNotification called for ${notificationsToSave.size} deals.")
            } else {
                 Log.d("PushNotificationService", "Parsed deal list is empty, no notification will be shown.")
            }
        }
    }
}
