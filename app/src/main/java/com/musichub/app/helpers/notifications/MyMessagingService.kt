package com.musichub.app.helpers.notifications

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.musichub.app.ui.activity.SplashActivity
import java.util.*

class MyMessagingService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        sentNotification(p0)
    }

    private fun sentNotification(remoteMessage: RemoteMessage) {
        var tittle = "MusicHub"
        var body = "Notification"
        var albumId = ""
        if (remoteMessage.data.isNotEmpty()) {
            tittle = remoteMessage.data["title"]!!
            body = remoteMessage.data["body"]!!
            albumId = remoteMessage.data["albumId"]!!
            Log.d("notificationPayLoad", remoteMessage.data.toString())
        } else {
            tittle = remoteMessage.notification!!.title!!
            body = remoteMessage.notification!!.body!!
            Log.d("notificationData", remoteMessage.data.toString())
        }
        val intent = Intent(this, SplashActivity::class.java)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntentWithParentStack(intent)
        intent.putExtra("tittle", tittle)
        intent.putExtra("body", body)
        intent.putExtra("albumId", albumId)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        val j = Random().nextInt()
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val latest = LatestDeviceNotification(this)
            val builder = latest.getLatest(tittle, body, pendingIntent, defaultSound)
            var i = 0
            if (j > 0) {
                i = j
            }
            latest.manager!!.notify(i, builder.build())
        }

    }
}