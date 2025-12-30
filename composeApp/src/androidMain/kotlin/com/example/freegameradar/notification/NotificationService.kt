package com.example.freegameradar.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import coil3.BitmapImage
import coil3.DrawableImage
import coil3.imageLoader
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import com.example.freegameradar.MainActivity
import com.example.freegameradar.data.model.DealNotification
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

    fun showNewDealsNotification(deals: List<DealNotification>) {
        if (deals.isEmpty()) return

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

                val title = "Psst! A free game is hiding nearby!"
                val text = "${deal.title} is free right now. Go get your loot!"

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
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
                val title = "Your radar is buzzing with new loot!"
                val text = "Found $dealCount new treasures for your game library!"

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
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
                        else -> null  // <- This fixes the type mismatch
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
