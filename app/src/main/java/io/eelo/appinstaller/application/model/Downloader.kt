package io.eelo.appinstaller.application.model

import android.content.Context
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Constants
import java.io.*
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import android.support.v4.app.NotificationCompat
import io.eelo.appinstaller.R
import android.app.NotificationChannel
import android.app.NotificationManager
import io.eelo.appinstaller.utils.Common

class Downloader {
    private var count = 0
    private var total = 0
    private val listeners = ArrayList<(Int, Int) -> Unit>()

    private val notifier = ThreadedListeners {
        listeners.forEach { it.invoke(count, total) }
    }

    private lateinit var notificationBuilder: NotificationCompat.Builder
    private lateinit var notificationManager: NotificationManager

    @Throws(IOException::class)
    fun download(context: Context, data: FullData, apkFile: File) {
        createApkFile(apkFile)
        // TODO Handle this error better, ideally do not create the APK file
        if (data.getLastVersion() != null) {
            val url = URL(Constants.DOWNLOAD_URL + data.getLastVersion()!!.downloadLink)
            val connection = url.openConnection() as HttpsURLConnection
            total = connection.contentLength
            initialiseNotification(context, data)
            transferBytes(connection, apkFile)
            connection.disconnect()
        }
    }

    private fun createApkFile(apkFile: File) {
        if (apkFile.exists()) {
            apkFile.delete()
        }
        apkFile.parentFile.mkdirs()
        apkFile.createNewFile()
        apkFile.deleteOnExit()
    }

    private fun initialiseNotification(context: Context, fullData: FullData) {
        notificationBuilder = if (android.os.Build.VERSION.SDK_INT >=
                android.os.Build.VERSION_CODES.O) {
            NotificationCompat.Builder(context, Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
        } else {
            NotificationCompat.Builder(context)
        }

        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(fullData.basicData.name)
                .setContentText(context.getString(R.string.download_notification_description))
                .setProgress(total, count, false)
                .setChannelId(Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID)
                .setOngoing(true)

        notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(Constants.DOWNLOAD_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.download_notification_channel_title),
                    NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationManager.notify(Constants.DOWNLOAD_NOTIFICATION_ID, notificationBuilder.build())
    }

    @Throws(IOException::class)
    private fun transferBytes(connection: HttpsURLConnection, apkFile: File) {
        connection.inputStream.use { input ->
            FileOutputStream(apkFile).use { output ->
                notifier.start()
                val buffer = ByteArray(1024)
                while (readAndWrite(input, output, buffer)) {
                    notificationBuilder.setProgress(total, count, false)
                    notificationBuilder.setSubText(((Common.toMiB(count) / Common.toMiB(total))
                            * 100).toInt().toString() + "%")
                    notificationManager.notify(Constants.DOWNLOAD_NOTIFICATION_ID,
                            notificationBuilder.build())
                }
            }
        }
        notificationManager.cancel(Constants.DOWNLOAD_NOTIFICATION_ID)
        notifier.stop()
    }

    @Throws(IOException::class)
    private fun readAndWrite(input: InputStream, output: OutputStream, buffer: ByteArray): Boolean {
        val count = input.read(buffer)
        if (count == -1) {
            return false
        }
        output.write(buffer, 0, count)
        this.count += count
        return true
    }

    fun addListener(listener: (Int, Int) -> Unit) {
        listeners.add(listener)
    }
}
