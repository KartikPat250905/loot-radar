package com.example.freegameradar.data.model

data class DealNotification(
    val id: Long,
    val title: String,
    val worth: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
