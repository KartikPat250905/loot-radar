package com.example.freegameradar.notification

import android.util.Log
import com.example.freegameradar.FreeGameRadarApp
import com.example.freegameradar.data.model.DealNotification
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushNotificationService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d("PushNotificationService", "From: ${remoteMessage.from}")

        remoteMessage.data["deal_ids"]?.let { dealIdsStr ->
            Log.d("PushNotificationService", "Received deal IDs: $dealIdsStr")

            val dealIds = dealIdsStr.split(",").mapNotNull { it.toLongOrNull() }
            if (dealIds.isEmpty()) {
                Log.d("PushNotificationService", "No valid deal IDs received.")
                return
            }

            // Fetch the full deals from Firestore in a background coroutine
            CoroutineScope(Dispatchers.IO).launch {
                fetchDealsAndNotify(dealIds)
            }
        }
    }

    private suspend fun fetchDealsAndNotify(dealIds: List<Long>) {
        try {
            val firestore = Firebase.firestore
            val chunkedDealIds = dealIds.chunked(30)
            val fetchedDeals = mutableListOf<DealNotification>()

            for (chunk in chunkedDealIds) {
                val documents = firestore.collection("deals")
                    .whereIn("id", chunk)
                    .get()
                    .await()

                val dealsFromChunk = documents.mapNotNull { doc ->
                    try {
                        DealNotification(
                            id = doc.getLong("id")!!,
                            title = doc.getString("title") ?: "",
                            worth = doc.getString("worth") ?: "N/A", // Fetch the new worth field
                            description = doc.getString("description") ?: "",
                            url = doc.getString("open_giveaway_url") ?: "",
                            imageUrl = doc.getString("image") ?: "",
                            timestamp = System.currentTimeMillis(),
                            isRead = false
                        )
                    } catch (e: Exception) {
                        Log.e("PushNotificationService", "Failed to parse deal document: ${doc.id}", e)
                        null
                    }
                }
                fetchedDeals.addAll(dealsFromChunk)
            }

            if (fetchedDeals.isNotEmpty()) {
                Log.d("PushNotificationService", "Successfully fetched ${fetchedDeals.size} deals from Firestore.")

                val repository = FreeGameRadarApp.instance.notificationRepository
                repository.saveNotifications(fetchedDeals)
                Log.d("PushNotificationService", "Saved ${fetchedDeals.size} deals to local DB.")

                val notificationService = NotificationService(applicationContext)
                notificationService.showNewDealsNotification(fetchedDeals)
                Log.d("PushNotificationService", "Displayed system notification.")

            } else {
                Log.d("PushNotificationService", "Could not find matching deals in Firestore for given IDs.")
            }
        } catch (e: Exception) {
            Log.e("PushNotificationService", "Error fetching deals from Firestore", e)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO: Update server with new token
    }
}
