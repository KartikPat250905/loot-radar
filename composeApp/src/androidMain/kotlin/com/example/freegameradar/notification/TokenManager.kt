package com.example.freegameradar.notification

import android.util.Log
import com.google.firebase.auth.ktx.auth
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

    fun updateFCMToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            saveTokenToFirestore(token)
        }
    }

    private suspend fun saveTokenToFirestore(token: String) {
        val uid = Firebase.auth.currentUser?.uid

        if (uid == null) {
            Log.w("TokenManager", "⚠️ User not logged in yet, cannot save token")
            return
        }

        val firestore = Firebase.firestore

        try {
            val userDocRef = firestore.collection("users").document(uid)

            // This will fail if the document does not exist, and the catch block will execute.
            userDocRef.update("notificationTokens", listOf(token)).await()
            Log.d("TokenManager", "✅ Token updated in Firestore for user: $uid")

        } catch (e: Exception) {
            Log.w("TokenManager", "⚠️ Document doesn't exist, creating new one with token.", e)

            // Document doesn't exist, create it
            try {
                val userDocRef = firestore.collection("users").document(uid)
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
