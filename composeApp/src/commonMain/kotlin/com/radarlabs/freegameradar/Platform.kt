package com.radarlabs.freegameradar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform