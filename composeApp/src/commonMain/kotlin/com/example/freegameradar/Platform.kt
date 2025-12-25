package com.example.freegameradar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform