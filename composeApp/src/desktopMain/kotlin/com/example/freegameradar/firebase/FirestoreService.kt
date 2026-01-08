package com.example.freegameradar.firebase

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * Desktop Firestore client using REST API
 * Matches Android Firebase Firestore SDK functionality
 */
class FirestoreService {
    
    private val client = FirebaseHttpClient.client
    
    /**
     * Get user document from Firestore
     * Returns default UserDocument if not found (404)
     */
    suspend fun getUserDocument(
        userId: String,
        idToken: String
    ): Result<UserDocument> {
        return try {
            val url = FirebaseConfig.getUserDocumentUrl(userId)
            println("📥 Fetching Firestore document: users/$userId")
            
            val response: HttpResponse = client.get(url) {
                header("Authorization", "Bearer $idToken")
            }
            
            when {
                response.status.isSuccess() -> {
                    val firestoreDoc = response.body<FirestoreDocument>()
                    val userDoc = firestoreDoc.toUserDocument()
                    println("✅ Firestore document fetched: ${userDoc.claimedGameIds.size} games, " +
                           "${userDoc.preferredGamePlatforms.size} platforms")
                    Result.success(userDoc)
                }
                response.status == HttpStatusCode.NotFound -> {
                    println("ℹ️ No Firestore document found, returning defaults")
                    Result.success(UserDocument())
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    println("❌ Firestore GET failed: ${response.status} - $errorBody")
                    Result.failure(Exception("Firestore GET failed: ${response.status}"))
                }
            }
        } catch (e: Exception) {
            println("❌ Firestore GET exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Create or update complete user document in Firestore
     * Uses PATCH to merge with existing data
     */
    suspend fun setUserDocument(
        userId: String,
        idToken: String,
        userDocument: UserDocument
    ): Result<Unit> {
        return try {
            val url = FirebaseConfig.getUserDocumentUrl(userId)
            println("💾 Saving Firestore document: users/$userId")
            
            val response: HttpResponse = client.patch(url) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $idToken")
                setBody(userDocument.toFirestoreDocument())
            }
            
            if (response.status.isSuccess()) {
                println("✅ Firestore document saved successfully")
                Result.success(Unit)
            } else {
                val errorBody = response.bodyAsText()
                println("❌ Firestore SET failed: ${response.status} - $errorBody")
                Result.failure(Exception("Firestore SET failed: ${response.status}"))
            }
        } catch (e: Exception) {
            println("❌ Firestore SET exception: ${e.message}")
            e.printStackTrace()
            Result.failure(e)
        }
    }
    
    /**
     * Delete user document from Firestore
     * Used during account deletion cleanup
     * Does NOT fail if document doesn't exist (idempotent)
     */
    suspend fun deleteUserDocument(
        userId: String,
        idToken: String
    ): Result<Unit> {
        return try {
            val url = FirebaseConfig.getUserDocumentUrl(userId)
            println("🗑️ Deleting Firestore document: users/$userId")
            
            val response: HttpResponse = client.delete(url) {
                header("Authorization", "Bearer $idToken")
            }
            
            when {
                response.status.isSuccess() -> {
                    println("✅ Firestore document deleted")
                    Result.success(Unit)
                }
                response.status == HttpStatusCode.NotFound -> {
                    println("ℹ️ Firestore document already deleted or never existed")
                    Result.success(Unit) // Treat as success
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    println("⚠️ Firestore DELETE failed: ${response.status} - $errorBody")
                    // Don't block account deletion if Firestore fails
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            println("⚠️ Firestore DELETE exception: ${e.message}")
            // Don't block account deletion if Firestore fails
            Result.success(Unit)
        }
    }
}