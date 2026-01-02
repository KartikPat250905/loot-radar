package com.example.freegameradar.notification

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object TokenManager {

    fun initializeFCMToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("TokenManager", "✅ Retrieved FCM token: $token")
                saveTokenToFirestore(token)
            } catch (e: Exception) {
                Log.e("TokenManager", "❌ Failed to get FCM token", e)
            }
        }
    }

    private suspend fun saveTokenToFirestore(token: String) {
        val uid = Firebase.auth.currentUser?.uid

        if (uid == null) {
            Log.w("TokenManager", "⚠️ User not logged in yet, cannot save token")
            return
        }

        try {
            val firestore = Firebase.firestore
            val userDocRef = firestore.collection("users").document(uid)

            // Try to update existing document
            userDocRef.update("notificationTokens", FieldValue.arrayUnion(token)).await()
            Log.d("TokenManager", "✅ Token saved to Firestore for user: $uid")

            // Verify it was saved
            val doc = userDocRef.get().await()
            val tokens = doc.get("notificationTokens") as? List<*>
            Log.d("TokenManager", "📋 Current tokens in Firestore: $tokens")

        } catch (e: Exception) {
            Log.w("TokenManager", "⚠️ Document doesn't exist, creating new one", e)

            // Document doesn't exist, create it
            try {
                val userDocRef = Firebase.firestore.collection("users").document(uid)
                val data = mapOf(
                    "notificationTokens" to listOf(token),
                    "notificationsEnabled" to true,
                    "preferredGamePlatforms" to listOf("pc", "steam", "epic-games-store")
                )
                userDocRef.set(data).await()
                Log.d("TokenManager", "✅ Created new user document with token")
            } catch (e2: Exception) {
                Log.e("TokenManager", "❌ Failed to create user document", e2)
            }
        }
    }
}
