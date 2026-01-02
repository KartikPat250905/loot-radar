package com.example.freegameradar.settings

import com.google.firebase.firestore.Exclude
import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val notificationsEnabled: Boolean = false,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList(),
    val setupComplete: Boolean = false
) {
    @get:Exclude
    var notificationTokens: List<String> = emptyList()
}
