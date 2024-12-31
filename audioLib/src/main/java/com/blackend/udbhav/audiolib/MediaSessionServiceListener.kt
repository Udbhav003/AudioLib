package com.blackend.udbhav.audiolib

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSessionService.Listener

@UnstableApi
class MediaSessionServiceListener(
    private val context: Context
) : Listener {

    @SuppressLint("MissingPermission")
    override fun onForegroundServiceStartNotAllowedException() {
        val notificationManagerCompat = NotificationManagerCompat.from(context)
        ensureNotificationChannel(notificationManagerCompat)
        val pendingIntent =
            TaskStackBuilder.create(context).run {
                getPendingIntent(0, IMMUTABLE_FLAG or PendingIntent.FLAG_UPDATE_CURRENT)
            }
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentIntent(pendingIntent)
                .setSmallIcon(com.facebook.react.R.drawable.ic_resume)
                .setContentTitle("Sample Notification Title")
                .setStyle(
                    NotificationCompat.BigTextStyle().bigText("Sample Notification Content")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val foregroundServiceBehavior = Notification.FOREGROUND_SERVICE_IMMEDIATE
            builder.foregroundServiceBehavior = foregroundServiceBehavior
        }
        notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }

    /**
     * Create a notification if not exist
     */
    private fun ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
        if (notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }
        val channel =
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManager.IMPORTANCE_DEFAULT)
                .setName("Sample Notification Title")
                .build()
        notificationManagerCompat.createNotificationChannel(channel)
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "audio_lib_notification_channel_id"
        private const val IMMUTABLE_FLAG = PendingIntent.FLAG_IMMUTABLE
    }
}