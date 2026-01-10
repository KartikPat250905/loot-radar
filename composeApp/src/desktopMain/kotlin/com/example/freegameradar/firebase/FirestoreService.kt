package com.example.freegameradar.firebase

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class FirestoreService {
    
    private val client = FirebaseHttpClient.client

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
                    println("✅ Firestore document fetched: ${userDoc.preferredGamePlatforms.size} platforms")
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
                    Result.success(Unit)
                }
                else -> {
                    val errorBody = response.bodyAsText()
                    println("⚠️ Firestore DELETE failed: ${response.status} - $errorBody")
                    Result.success(Unit)
                }
            }
        } catch (e: Exception) {
            println("⚠️ Firestore DELETE exception: ${e.message}")
            Result.success(Unit)
        }
    }
}