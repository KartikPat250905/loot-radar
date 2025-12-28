package com.example.freegameradar.settings

import kotlinx.serialization.Serializable

@Serializable
data class UserSettings(
    val preferredPlatform: String = "PC",
    val currency: String = "USD",
    val notificationsEnabled: Boolean = true
)
