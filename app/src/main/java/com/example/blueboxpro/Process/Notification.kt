package com.example.blueboxpro.Process

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.blueboxpro.MainActivity

/**
 * Utility for managing application notifications and channels.
 */
object NotificationHelper {
    const val CHANNEL_ID = "bluebox_service_channel"
    private const val CHANNEL_NAME = "BlueBox Background Service"
    const val NOTIFICATION_ID = 1

    /**
     * Creates the notification channel required for Android O and above.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Running in background to track movement"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    /**
     * Builds the foreground service notification.
     */
    fun buildNotification(
        context: Context,
        speed: String = "--",
        heading: String = "--",
        wind: String = "--"
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val contentText = "Vitesse: $speed | Cap: $heading | Vent: $wind"

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("BlueBox Pro - En cours")
            .setContentText(contentText)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation) 
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Updates an existing notification.
     */
    fun updateNotification(context: Context, speed: String, heading: String, wind: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification(context, speed, heading, wind))
    }
}
