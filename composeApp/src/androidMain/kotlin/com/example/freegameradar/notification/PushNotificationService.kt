package com.example.freegameradar.notification

import android.util.Log
import com.example.freegameradar.FreeGameRadarApp
import com.example.freegameradar.data.model.DealNotification
import com.example.freegameradar.data.remote.ApiService
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class PushNotificationService : FirebaseMessagingService() {

    private val apiService by lazy { ApiService() }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("FCM", "=================================")
        Log.d("FCM", "Message received from: ${remoteMessage.from}")
        Log.d("FCM", "Data payload: ${remoteMessage.data}")
        Log.d("FCM", "=================================")

        remoteMessage.data["deal_ids"]?.let { dealIds ->
            Log.d("FCM", "Received deal IDs: $dealIds")

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    fetchAndShowDeals(dealIds)
                } catch (e: Exception) {
                    Log.e("FCM", "Error processing deal IDs", e)
                    e.printStackTrace()
                }
            }
        } ?: run {
            Log.w("FCM", "No 'deal_ids' field in data payload!")
        }
    }

    private suspend fun fetchAndShowDeals(dealIds: String) {
        val ids = dealIds.split(",").mapNotNull { it.trim().toLongOrNull() }
        if (ids.isEmpty()) {
            Log.w("FCM", "Deal IDs list is empty or invalid")
            return
        }

        // Fetch deals concurrently to avoid overwhelming the API
        val deals = coroutineScope {
            ids.map { id ->
                async {
                    try {
                        apiService.getGameById(id.toString())
                    } catch (e: Exception) {
                        Log.e("FCM", "Failed to fetch deal with ID: $id", e)
                        null
                    }
                }
            }.awaitAll().filterNotNull()
        }


        if (deals.isEmpty()) {
            Log.w("FCM", "Failed to fetch any deals from the API")
            return
        }

        val notifications = deals.mapNotNull { deal ->
            deal.id?.let {
                DealNotification(
                    id = it,
                    title = deal.title ?: "No Title",
                    description = deal.description ?: "",
                    url = deal.open_giveaway_url ?: "",
                    imageUrl = deal.image ?: "",
                    worth = deal.worth ?: "N/A",
                    timestamp = System.currentTimeMillis(),
                    isRead = false
                )
            }
        }

        if (notifications.isNotEmpty()) {
            // Save to local database
            val repository = FreeGameRadarApp.instance.notificationRepository
            repository.saveNotifications(notifications)
            Log.d("FCM", "✅ Saved ${notifications.size} notifications to local DB")

            // Show system notification
            val notificationService = NotificationService(applicationContext)
            notificationService.createNotificationChannel()
            notificationService.showNewDealsNotification(notifications)
            Log.d("FCM", "✅ Displayed system notification for ${notifications.size} deals")
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "🔑 New FCM token generated: $token")
        // If you have a token manager, you should re-initialize it here.
        // TokenManager.initializeFCMToken()
    }
}
