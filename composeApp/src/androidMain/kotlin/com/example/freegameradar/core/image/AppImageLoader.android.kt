package com.example.freegameradar.core.image

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy

actual object AppImageLoader {
    actual fun get(context: Any): ImageLoader {
        val androidContext = context as Context
        return ImageLoader.Builder(androidContext)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(androidContext, 0.20)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(androidContext.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512 mb cache
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}
