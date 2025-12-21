package com.example.lootradarkmp.data.models

data class User(
    val uid: String,
    val email: String? = null,
    val isGuest: Boolean = false
)
