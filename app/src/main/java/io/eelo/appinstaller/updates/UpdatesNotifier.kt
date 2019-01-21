package io.eelo.appinstaller.updates

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import io.eelo.appinstaller.R
import io.eelo.appinstaller.utils.Constants

class UpdatesNotifier {
    private fun getNotification(context: Context, numberOfApps: Int, installAutomatically: Boolean):
            Notification {
        val notificationBuilder =
                NotificationCompat.Builder(context, Constants.UPDATES_NOTIFICATION_CHANNEL_ID)
        notificationBuilder.setSmallIcon(R.drawable.ic_app_updated_on)
                .priority = NotificationCompat.PRIORITY_DEFAULT
        if (numberOfApps == 1) {
            notificationBuilder.setContentTitle(context.resources.getQuantityString(
                    R.plurals.updates_notification_title,
                    1,
                    numberOfApps))
        } else {
            notificationBuilder.setContentTitle(context.resources.getQuantityString(
                    R.plurals.updates_notification_title,
                    numberOfApps,
                    numberOfApps))
        }
        if (installAutomatically) {
            notificationBuilder.setContentText(context.getString(R.string.updates_notification_text,
                    Constants.AUTOMATICALLY_INSTALL_UPDATES))
        } else {
            notificationBuilder.setContentText(context.getString(R.string.updates_notification_text,
                    Constants.MANUALLY_INSTALL_UPDATES))
        }
        return notificationBuilder.build()
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                    Constants.UPDATES_NOTIFICATION_CHANNEL_ID,
                    Constants.UPDATES_NOTIFICATION_CHANNEL_TITLE,
                    importance)
            val notificationManager: NotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as
                            NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showNotification(context: Context, numberOfApps: Int, installAutomatically: Boolean) {
        with(NotificationManagerCompat.from(context)) {
            createNotificationChannel(context)
            notify(Constants.UPDATES_NOTIFICATION_ID,
                    getNotification(context, numberOfApps, installAutomatically))
        }
    }
}
