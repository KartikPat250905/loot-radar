package com.example.lootradarkmp.core.image

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

actual object AppImageLoader {

    actual fun get(): ImageLoader {
        val cacheDir = NSFileManager.defaultManager
            .URLsForDirectory(
                directory = NSDocumentDirectory,
                inDomains = NSUserDomainMask
            )
            .first() as platform.Foundation.NSURL

        val cachePath = cacheDir.path!!.toPath()

        return ImageLoader.Builder()
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(0.15) // Smaller memory cache
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cachePath.resolve("image_cache"))
                    .maxSizeBytes(512L * 1024 * 1024) // 512 MB disk cache
                    .build()
            }
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .build()
    }
}