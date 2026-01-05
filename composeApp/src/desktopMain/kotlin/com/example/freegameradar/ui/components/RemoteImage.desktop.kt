package com.example.freegameradar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest

@Composable
actual fun RemoteImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier,
    contentScale: ContentScale,
    placeholder: @Composable (() -> Unit)?,
    error: @Composable (() -> Unit)?
) {
    val context = LocalPlatformContext.current

    if (url.isNullOrEmpty()) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text("🎮", style = MaterialTheme.typography.headlineSmall)
        }
        return
    }

    println("Loading image: $url") // Debug log

    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(url)
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        onLoading = {
            println("Image loading: $url")
        },
        onSuccess = {
            println("Image loaded successfully: $url")
        },
        onError = {
            println("Image failed to load: $url - ${it.result.throwable.message}")
        }
    )
}
