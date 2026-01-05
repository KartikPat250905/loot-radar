package com.example.freegameradar.core.image

import android.content.Context
import coil3.ImageLoader
import coil3.network.ktor.KtorNetworkFetcherFactory

actual object AppImageLoader {
    actual fun get(context: Any): ImageLoader {
        require(context is Context) { "Context must be an Android Context" }
        return ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory())
            }
            .build()
    }
}
