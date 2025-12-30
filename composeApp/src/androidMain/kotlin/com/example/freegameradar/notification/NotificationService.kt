package com.example.freegameradar.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.freegameradar.MainActivity
import com.example.freegameradar.data.model.DealNotification

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

        val dealCount = deals.size
        val title = if (dealCount == 1) {
            "Your Loot Radar is beeping!"
        } else {
            "Heads up, new loot spotted!"
        }

        val body = if (dealCount == 1) {
            deals.first().title
        } else {
            "You\'ve got $dealCount new free game deals waiting for you!"
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("route", "notification")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a system icon to avoid build errors
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setGroup(GROUP_KEY_DEALS)
            .setAutoCancel(true)

        if (dealCount > 1) {
            val inboxStyle = NotificationCompat.InboxStyle()
                .setBigContentTitle("$dealCount New Deals")
            deals.forEach { deal -> inboxStyle.addLine(deal.title) }
            notificationBuilder.setStyle(inboxStyle)
        }

        val summaryNotification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("New Deals")
            .setContentText("$dealCount new deals")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using a system icon to avoid build errors
            .setGroup(GROUP_KEY_DEALS)
            .setGroupSummary(true)
            .build()

        notificationManager.notify(NEW_DEALS_NOTIFICATION_ID, notificationBuilder.build())
        notificationManager.notify(SUMMARY_NOTIFICATION_ID, summaryNotification)
    }

    companion object {
        const val CHANNEL_ID = "new_deals_channel"
        private const val NEW_DEALS_NOTIFICATION_ID = 1
        private const val SUMMARY_NOTIFICATION_ID = 0
        private const val GROUP_KEY_DEALS = "com.example.freegameradar.DEALS"
    }
}
