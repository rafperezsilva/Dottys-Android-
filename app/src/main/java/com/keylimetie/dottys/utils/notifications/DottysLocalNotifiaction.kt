package com.keylimetie.dottys.utils.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.keylimetie.dottys.DottysBaseActivity
import com.keylimetie.dottys.DottysMainNavigationActivity
import com.keylimetie.dottys.R
import com.keylimetie.dottys.utils.currentDateTime

data class DottysNotificationModel (
    var title: String = String(),
    var subTitle: String = String(),
    var description: String = String()

)
class DottysLocalNotifiaction(private val baseActivity: DottysBaseActivity, notificatioData: DottysNotificationModel)  {
    var channelID = String().currentDateTime()
    val idNotification = (1001 .. 9999).random()
init {
    createNotificationChannel()
    val intent = Intent(baseActivity, DottysMainNavigationActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    val pendingIntent = PendingIntent.getActivity(baseActivity, 0, intent, 0)
    val mBuilder = NotificationCompat.Builder(baseActivity, channelID)
        .setSmallIcon(R.mipmap.dottys_notification_icon)
        .setContentTitle(notificatioData.title)
        .setContentText(notificatioData.subTitle)
        .setStyle(NotificationCompat.BigTextStyle()
            .bigText(notificatioData.description))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setContentIntent(pendingIntent)

    // Step 4: Show the notification
    val notificationManager = NotificationManagerCompat.from(baseActivity)
    // notificationId is a unique int for each notification that you must define
    notificationManager.notify(idNotification, mBuilder.build())

}
    // Step 2: Create a channel and set the importance
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name =  "R.string.channel_name"
            val description =  "R.string.channel_description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = baseActivity.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

}
