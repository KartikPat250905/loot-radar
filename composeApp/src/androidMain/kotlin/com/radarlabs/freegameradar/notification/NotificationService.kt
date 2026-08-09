package com.radarlabs.freegameradar.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import coil3.BitmapImage
import coil3.DrawableImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.radarlabs.freegameradar.MainActivity
import com.radarlabs.freegameradar.R
import com.radarlabs.freegameradar.data.model.DealNotification
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationService(private val context: Context) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "New Game Deals",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Receive notifications for new free game deals."
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationService", "⚠️ POST_NOTIFICATIONS permission not granted. Notification will be suppressed.")
                return false
            }
        }
        return true
    }

    fun showNewDealsNotification(deals: List<DealNotification>) {
        if (!checkPermission() || deals.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            val dealCount = deals.size
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                putExtra("route", "notification")
            }

            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (dealCount == 1) {
                val deal = deals.first()
                val bitmap = fetchImage(deal.imageUrl)

                val title = "🎯 Target Acquired: Free Game Detected"
                val text = "${deal.title} is in range. Claim it before the signal fades!"

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(context.getColor(R.color.green))
                    .setContentTitle(title)
                    .setContentText(text)
                    .setLargeIcon(bitmap)
                    .setStyle(NotificationCompat.BigTextStyle().bigText(deal.description))
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(deal.id.toInt(), notification)

            } else {
                val title = "📡 Radar Storm: $dealCount Free Games!"
                val text = "Your scanner is lighting up! Open now to claim them all."

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setColor(context.getColor(R.color.green))
                    .setContentTitle(title)
                    .setContentText(text)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
            }
        }
    }

    fun showFallbackNotification(count: Int) {
        if (!checkPermission()) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("route", "notification")
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val title = "📡 Radar Ping: New Signals Detected"
        val text = "We found $count new free games! Tap to check the radar."

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(context.getColor(R.color.green))
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(SUMMARY_NOTIFICATION_ID, notification)
    }

    private suspend fun fetchImage(url: String): Bitmap? {
        if (url.isBlank()) return null
        return withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(url)
                .build()

            try {
                val result = context.imageLoader.execute(request)
                if (result is SuccessResult) {
                    when (val image = result.image) {
                        is BitmapImage -> image.bitmap
                        is DrawableImage -> {
                            val drawable = image.drawable
                            if (drawable is BitmapDrawable) {
                                drawable.bitmap
                            } else {
                                val bitmap = Bitmap.createBitmap(
                                    drawable.intrinsicWidth.coerceAtLeast(1),
                                    drawable.intrinsicHeight.coerceAtLeast(1),
                                    Bitmap.Config.ARGB_8888
                                )
                                val canvas = Canvas(bitmap)
                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                drawable.draw(canvas)
                                bitmap
                            }
                        }
                        else -> null
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("NotificationService", "Failed to load image for notification", e)
                null
            }
        }
    }


    companion object {
        const val CHANNEL_ID = "new_deals_channel"
        private const val SUMMARY_NOTIFICATION_ID = -1001
    }
}
