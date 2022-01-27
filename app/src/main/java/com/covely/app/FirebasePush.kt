package com.covely.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebasePush : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("D_MyFirebaseMessaging", "onMessageReceived: ${remoteMessage}");


        val data=remoteMessage.data
        showNotification(this,data)
        var install_id =""
        var notification_id=""
        var sent_at=""
        var sent_id=""

        if (data.containsKey("install_id")) install_id=data["install_id"]!!

        if (data.containsKey("notification_id")) notification_id=data["notification_id"]!!

        if (data.containsKey("sent_at")) sent_at = data["sent_at"]!!

        if (data.containsKey("sent_id")) sent_id=data["sent_id"]!!

        super.onMessageReceived(remoteMessage)
    }
    fun showNotification(context: Context?, data: Map<String,String>) {
        val CHANNEL_ID = context!!.getString(R.string.app_name)
        val CHANNEL_NAME = context!!.getString(R.string.app_name)
        val notificationIntent = Intent(context, MainActivity::class.java)


        if (data.containsKey("install_id"))  notificationIntent.putExtra("install_id", data["install_id"])

        if (data.containsKey("notification_id")) notificationIntent.putExtra("notification_id", data["notification_id"])

        if (data.containsKey("sent_at")) notificationIntent.putExtra("sent_at", data["sent_at"])

        if (data.containsKey("sent_id")) notificationIntent.putExtra("sent_id", data["sent_id"])

        if (data.containsKey("action_url")) notificationIntent.putExtra("action_url", data["action_url"])


        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val pendingIntent = PendingIntent.getActivity(context, 0,
            notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val defaultChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(defaultChannel)
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder.setSmallIcon(R.mipmap.ic_launcher)
        } else {
            builder.setSmallIcon(R.mipmap.ic_launcher)
        }
        var title=context!!.getString(R.string.app_name)
        var body=context!!.getString(R.string.app_name)

        if (data.containsKey("title")) title=data["title"]!!

        if (data.containsKey("body")) title=data["body"]!!

        builder.setStyle(NotificationCompat.BigTextStyle().bigText(title))
            .setSmallIcon(R.mipmap.ic_launcher).setContentTitle(title)
            .setPriority(Notification.PRIORITY_MAX)
            .setContentText(body).setAutoCancel(true).setSound(soundUri)
            .setContentIntent(pendingIntent)
        val notification = builder.build()
        notification.defaults = notification.defaults or Notification.DEFAULT_VIBRATE
        notification.flags = notification.flags or Notification.FLAG_SHOW_LIGHTS
        notificationManager.notify(1, notification)
    }
}