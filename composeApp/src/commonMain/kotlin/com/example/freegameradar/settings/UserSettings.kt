package com.example.freegameradar.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val notificationsEnabled: Boolean = true,
    val preferredGamePlatforms: List<String> = emptyList(),
    val preferredGameTypes: List<String> = emptyList(),
    val notificationTokens: List<String> = emptyList()
)
