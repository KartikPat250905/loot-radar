package com.example.freegameradar.fcm

import android.util.Log
import com.example.freegameradar.FreeGameRadarApp
import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.notification.NotificationService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

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

class FCMService : FirebaseMessagingService() {

    private val json = Json { ignoreUnknownKeys = true }
    private val ioScope = CoroutineScope(Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FCMService", "Message received: ${remoteMessage.data}")

        val dealsJson = remoteMessage.data["deals"] ?: return

        val fcmDeals = try {
            json.decodeFromString<List<FcmDeal>>(dealsJson)
        } catch (_: Exception) {
            try {
                json.decodeFromString<DealsWrapper>(dealsJson).deals
            } catch (_: Exception) {
                try {
                    listOf(json.decodeFromString<FcmDeal>(dealsJson))
                } catch (e: Exception) {
                    Log.e("FCMService", "Failed to parse deals JSON", e)
                    return
                }
            }
        }

        if (fcmDeals.isEmpty()) return

        val notifications = fcmDeals.map {
            DealNotification(
                id = it.id,
                title = it.title,
                description = it.description,
                url = it.open_giveaway_url,
                imageUrl = it.image,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        }

        val repository = FreeGameRadarApp.instance.notificationRepository
        Log.d("FCMService", "Repo hash = ${repository.hashCode()}")

        ioScope.launch {
            repository.saveNotifications(notifications)
            Log.d("FCMService", "Inserted ${notifications.size} notifications into DB")
        }

        NotificationService(applicationContext)
            .showNewDealsNotification(notifications)
    }
}
