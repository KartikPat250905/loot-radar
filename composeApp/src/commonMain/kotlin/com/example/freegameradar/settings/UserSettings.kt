package com.example.freegameradar.settings

// Remove: import com.google.firebase.firestore.Exclude
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class UserSettings(
    val notificationsEnabled: Boolean = false,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList(),
    val setupComplete: Boolean = false
) {
    @Transient  // Changed from @get:Exclude
    var notificationTokens: List<String> = emptyList()
}
