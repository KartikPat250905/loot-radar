package com.example.freegameradar.core.image

import coil3.ImageLoader

expect object AppImageLoader {
    fun get(context: Any): ImageLoader
}
