package com.example.freegameradar.notification

import android.util.Log
import com.example.freegameradar.FreeGameRadarApp
import com.example.freegameradar.data.model.DealNotification
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class FcmDeal(
    val id: Long,
    val title: String,
    val description: String,
    val open_giveaway_url: String,
    val image: String,
    val worth: String? = null
)

class PushNotificationService : FirebaseMessagingService() {

    private val json by lazy { Json { ignoreUnknownKeys = true } }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "=================================")
        Log.d("FCM", "Message received from: ${remoteMessage.from}")
        Log.d("FCM", "Data payload: ${remoteMessage.data}")
        Log.d("FCM", "=================================")

        // Backend sends "deals" as JSON string of deal objects
        remoteMessage.data["deals"]?.let { dealsJson ->
            Log.d("FCM", "Received deals JSON (length: ${dealsJson.length})")
            Log.d("FCM", "First 300 chars: ${dealsJson.take(300)}")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    parseAndSaveDeals(dealsJson)
                } catch (e: Exception) {
                    Log.e("FCM", "Error processing deals", e)
                    e.printStackTrace()
                }
            }
        } ?: run {
            Log.w("FCM", "No 'deals' field in data payload!")
        }
    }

    private suspend fun parseAndSaveDeals(dealsJson: String) {
        try {
            // Parse the JSON array of deals
            val fcmDeals = json.decodeFromString(
                ListSerializer(FcmDeal.serializer()),
                dealsJson
            )

            Log.d("FCM", "Successfully parsed ${fcmDeals.size} deals")

            if (fcmDeals.isEmpty()) {
                Log.w("FCM", "Parsed deals list is empty")
                return
            }

            // Convert to DealNotification objects
            val notifications = fcmDeals.map { deal ->
                DealNotification(
                    id = deal.id,
                    title = deal.title,
                    description = deal.description,
                    url = deal.open_giveaway_url,
                    imageUrl = deal.image,
                    worth = deal.worth ?: "N/A",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            }

            // Save to local database
            val repository = FreeGameRadarApp.instance.notificationRepository
            repository.saveNotifications(notifications)
            Log.d("FCM", "✅ Saved ${notifications.size} notifications to local DB")

            // Show system notification
            val notificationService = NotificationService(applicationContext)
            notificationService.createNotificationChannel()
            notificationService.showNewDealsNotification(notifications)
            Log.d("FCM", "✅ Displayed system notification for ${notifications.size} deals")

        } catch (e: Exception) {
            Log.e("FCM", "❌ Failed to parse or save deals", e)
            e.printStackTrace()
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "🔑 New FCM token generated: $token")
        TokenManager.initializeFCMToken()
    }
}
