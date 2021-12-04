package com.musichub.app.helpers.notifications

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.musichub.app.helpers.PrefManager
import com.musichub.app.ui.activity.SplashActivity
import java.util.*

class MyMessagingService : FirebaseMessagingService() {
    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        if (PrefManager(this).isNotificationEnabled()) {
            sentNotification(p0)
        }
    }

    private fun sentNotification(remoteMessage: RemoteMessage) {
        var tittle = "MusicHub"
        var body = "Notification"
        if (remoteMessage.data.isNotEmpty()) {
            tittle = remoteMessage.data["title"]!!
            body = remoteMessage.data["body"]!!
            Log.d("notificationPayLoad", remoteMessage.data.toString())
        } else {
            tittle = remoteMessage.notification!!.title!!
            body = remoteMessage.notification!!.body!!
            Log.d("notificationData", remoteMessage.data.toString())
        }
        val intent = Intent(this, SplashActivity::class.java)
        intent.putExtra("tittle", tittle)
        intent.putExtra("body", body)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val j = Random().nextInt()
        val pendingIntent = PendingIntent.getActivity(
            getApplicationContext(), j, intent,
            PendingIntent.FLAG_ONE_SHOT
        )

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