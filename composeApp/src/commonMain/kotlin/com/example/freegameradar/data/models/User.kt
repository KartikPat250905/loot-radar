package com.example.freegameradar.data.models

data class User(
    val uid: String,
    val email: String? = null,
    val isGuest: Boolean = false
)
