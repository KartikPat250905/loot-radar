package com.example.lootradarkmp.core.image

import android.content.Context
import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.util.DebugLogger

object AndroidContextHolder {
    lateinit var context: Context
}

actual object AppImageLoader {

    actual fun get(): ImageLoader {
        return ImageLoader.Builder(AndroidContextHolder.context)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(AndroidContextHolder.context, 0.20) // Smaller memory cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(AndroidContextHolder.context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512 mb cache
                    .build()
            }
            // This is critical - READ_ONLY means it will read from disk when offline
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}