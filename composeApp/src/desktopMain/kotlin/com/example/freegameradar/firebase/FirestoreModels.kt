package com.example.freegameradar.firebase

import kotlinx.serialization.Serializable

/**
 * Firestore REST API document structure
 * Docs: https://firebase.google.com/docs/firestore/reference/rest/v1/projects.databases.documents
 */
@Serializable
data class FirestoreDocument(
    val name: String? = null,
    val fields: Map<String, FirestoreValue>? = null,
    val createTime: String? = null,
    val updateTime: String? = null
)

@Serializable
data class FirestoreValue(
    val stringValue: String? = null,
    val integerValue: String? = null,
    val booleanValue: Boolean? = null,
    val doubleValue: Double? = null,
    val arrayValue: ArrayValue? = null,
    val mapValue: MapValue? = null
)

@Serializable
data class ArrayValue(
    val values: List<FirestoreValue>? = null
)

@Serializable
data class MapValue(
    val fields: Map<String, FirestoreValue>? = null
)

/**
 * User document model matching Android Firestore structure
 * Fields: notificationTokens, notificationsEnabled, 
 *         preferredGamePlatforms, preferredGameTypes, setupComplete
 */
@Serializable
data class UserDocument(
    val notificationTokens: List<String> = emptyList(), // Desktop won't use FCM
    val notificationsEnabled: Boolean = false,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList(),
    val setupComplete: Boolean = false
)

/**
 * Convert UserDocument to Firestore REST API format
 */
fun UserDocument.toFirestoreDocument(): FirestoreDocument {
    return FirestoreDocument(
        fields = mapOf(
            "notificationTokens" to FirestoreValue(
                arrayValue = ArrayValue(
                    values = notificationTokens.map { FirestoreValue(stringValue = it) }
                )
            ),
            "notificationsEnabled" to FirestoreValue(booleanValue = notificationsEnabled),
            "preferredGamePlatforms" to FirestoreValue(
                arrayValue = ArrayValue(
                    values = preferredGamePlatforms.map { FirestoreValue(stringValue = it) }
                )
            ),
            "preferredGameTypes" to FirestoreValue(
                arrayValue = ArrayValue(
                    values = preferredGameTypes.map { FirestoreValue(stringValue = it) }
                )
            ),
            "setupComplete" to FirestoreValue(booleanValue = setupComplete)
        )
    )
}

/**
 * Parse Firestore REST document to UserDocument
 */
fun FirestoreDocument.toUserDocument(): UserDocument {
    val fields = this.fields ?: return UserDocument()
    
    return UserDocument(
        notificationTokens = fields["notificationTokens"]?.arrayValue?.values
            ?.mapNotNull { it.stringValue } ?: emptyList(),
        notificationsEnabled = fields["notificationsEnabled"]?.booleanValue ?: false,
        preferredGamePlatforms = fields["preferredGamePlatforms"]?.arrayValue?.values
            ?.mapNotNull { it.stringValue } ?: emptyList(),
        preferredGameTypes = fields["preferredGameTypes"]?.arrayValue?.values
            ?.mapNotNull { it.stringValue } ?: emptyList(),
        setupComplete = fields["setupComplete"]?.booleanValue ?: false
    )
}