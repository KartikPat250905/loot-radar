package com.example.lootradarkmp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform