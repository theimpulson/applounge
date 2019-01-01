package io.eelo.appinstaller.application.model

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES.O
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import io.eelo.appinstaller.R
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants

class DownloadNotification {

    private lateinit var notificationManager: NotificationManager
    private lateinit var builder: NotificationCompat.Builder

    fun show(total: Int, count: Int) {
        builder.setProgress(total, count, false)
        builder.setSubText(((Common.toMiB(count) / Common.toMiB(total)) * 100).toInt().toString() + "%")
        notificationManager.notify(Constants.DOWNLOAD_NOTIFICATION_ID, builder.build())
    }

    fun hide() {
        notificationManager.cancel(Constants.DOWNLOAD_NOTIFICATION_ID)
    }

    fun create(context: Context, appName: String) {
        val builder = createBuilder(context)

        builder.setSmallIcon(R.drawable.ic_notification_download)
                .setContentTitle(appName)
                .setContentText(context.getString(R.string.download_notification_description))
                .setChannelId(Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
                .setOngoing(true).build()

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (SDK_INT >= O) {
            createNotificationChannel(context)
        }
    }

    @RequiresApi(O)
    private fun createNotificationChannel(context: Context) {
        val mChannel = NotificationChannel(Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                context.getString(R.string.download_notification_channel_title),
                NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(mChannel)
    }

    private fun createBuilder(context: Context): NotificationCompat.Builder {
        return if (SDK_INT >= O) {
            NotificationCompat.Builder(context, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }
    }
}