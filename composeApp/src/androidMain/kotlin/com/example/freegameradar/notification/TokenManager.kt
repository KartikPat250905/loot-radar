package com.example.freegameradar.notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object TokenManager {

    private var isListenerAttached = false
    private var authStateListener: FirebaseAuth.AuthStateListener? = null

    fun initializeFCMToken() {
        if (isListenerAttached) return
        isListenerAttached = true

        val auth = Firebase.auth
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                Log.d("TokenManager", "Auth state changed: User signed in (UID: ${user.uid}, Anonymous: ${user.isAnonymous})")
                getAndSaveFCMToken()
            } else {
                Log.d("TokenManager", "Auth state changed: User signed out")
            }
        }

        auth.addAuthStateListener(authStateListener!!)
    }

    private fun getAndSaveFCMToken() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val uid = Firebase.auth.currentUser?.uid
                if (uid == null) {
                    Log.w("TokenManager", "⚠️ Cannot get FCM token - user not authenticated")
                    return@launch
                }

                val token = FirebaseMessaging.getInstance().token.await()
                Log.d("TokenManager", "✅ Retrieved FCM token: $token for user: $uid")
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
            Log.w("TokenManager", "⚠️ User not logged in, cannot save token")
            return
        }

        val firestore = Firebase.firestore
        val userDocRef = firestore.collection("users").document(uid)

        try {
            // Use a single set() operation with merge to handle both create and update
            // This ensures the document exists and adds the token in one atomic operation
            val tokenData = mapOf(
                "notificationTokens" to FieldValue.arrayUnion(token)
            )

            userDocRef.set(tokenData, SetOptions.merge()).await()

            Log.d("TokenManager", "✅ Token saved to Firestore for user: $uid")

        } catch (e: Exception) {
            Log.e("TokenManager", "❌ Failed to save token for UID: $uid")
            Log.e("TokenManager", "Error message: ${e.message}")
            e.printStackTrace()
        }
    }
}
