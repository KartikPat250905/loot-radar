package com.example.freegameradar.data.models

data class User(
    val uid: String,
    val email: String,
    val isAnonymous: Boolean = false
)
