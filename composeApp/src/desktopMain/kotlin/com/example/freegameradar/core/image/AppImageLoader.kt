package com.example.freegameradar.core.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.network.ktor.KtorNetworkFetcherFactory

actual object AppImageLoader {
    actual fun get(context: Any): ImageLoader {
        return ImageLoader.Builder(context as PlatformContext)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
}
