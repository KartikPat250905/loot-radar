package com.example.freegameradar.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.util.*

data class RedemptionResult(val success: Boolean, val message: String)

suspend fun validateAndRedeemCode(code: String): RedemptionResult {
    if (code.isBlank()) {
        return RedemptionResult(false, "Code cannot be empty.")
    }

    val db = Firebase.firestore
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser

    if (currentUser == null) {
        return RedemptionResult(false, "You must be logged in to redeem a code.")
    }

    return try {
        val codeDocRef = db.collection("supportCodes").document(code)
        val codeDoc = codeDocRef.get().await()

        if (!codeDoc.exists()) {
            return RedemptionResult(false, "Invalid code. Please check and try again.")
        }

        val isUsed = codeDoc.getBoolean("isUsed") ?: false
        if (isUsed) {
            val usedBy = codeDoc.getString("usedBy")
            if (usedBy == currentUser.uid) {
                return RedemptionResult(true, "You have already redeemed this code.")
            } else {
                return RedemptionResult(false, "This code has already been used by another account.")
            }
        }

        val days = (codeDoc.data?.get("days") as? Long)?.toInt() ?: 0
        val tier = codeDoc.data?.get("tier") as? String ?: "standard"

        // Calculate expiry date
        val calendar = Calendar.getInstance()

        // Check if user already has ad-free time remaining
        val userDoc = db.collection("users").document(currentUser.uid).get().await()
        if (userDoc.exists()) {
            val existingExpiry = userDoc.getTimestamp("adFreeUntil")?.toDate()
            if (existingExpiry != null && existingExpiry.after(Date())) {
                calendar.time = existingExpiry
            }
        }

        calendar.add(Calendar.DAY_OF_YEAR, days)
        val expiryDate = calendar.time

        // Get current timestamp for history
        val now = Date()

        val userDocRef = db.collection("users").document(currentUser.uid)

        // ✅ Use regular Date() instead of FieldValue.serverTimestamp() in arrayUnion
        db.runBatch { batch ->
            // Update code status
            batch.update(
                codeDocRef,
                "isUsed", true,
                "usedBy", currentUser.uid,
                "usedAt", FieldValue.serverTimestamp()
            )

            // Update user ad-free status
            batch.set(
                userDocRef,
                mapOf(
                    "adFreeUntil" to expiryDate,
                    "supportTier" to tier,
                    "supportHistory" to FieldValue.arrayUnion(
                        mapOf(
                            "code" to code,
                            "redeemedAt" to now,  // ✅ Use Date() instead of serverTimestamp
                            "tier" to tier
                        )
                    )
                ),
                SetOptions.merge()
            )
        }.await()

        val daysText = if (days > 3650) "lifetime" else "$days days"
        RedemptionResult(true, "✅ Success! You're ad-free for $daysText")

    } catch (e: Exception) {
        RedemptionResult(false, "An error occurred: ${e.message}")
    }
}
