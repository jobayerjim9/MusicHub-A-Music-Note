package com.musichub.app.helpers.notifications

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.ContextWrapper
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.musichub.app.R

class LatestDeviceNotification(base: Context?) :
    ContextWrapper(base) {
    var manager: NotificationManager? = null
        get() {
            if (field == null) {
                field = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val notificationChannel =
            NotificationChannel(Channel_Id, Channel_Name, NotificationManager.IMPORTANCE_HIGH)
        notificationChannel.enableLights(true)
        notificationChannel.enableVibration(true)
        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        notificationChannel.setSound(defaultSound, audioAttributes)

        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        manager!!.createNotificationChannel(notificationChannel)
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun getLatest(
        tittle: String?,
        body: String?,
        pendingIntent: PendingIntent?,
        sound: Uri?
    ): Notification.Builder {
        return Notification.Builder(applicationContext, Channel_Id)
            .setContentIntent(pendingIntent)
            .setContentTitle(tittle)
            .setContentText(body)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .setStyle(Notification.BigTextStyle())
    }

    companion object {
        private const val Channel_Id = "com.musichub.app"
        private const val Channel_Name = "MusicHub"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }
    }
}