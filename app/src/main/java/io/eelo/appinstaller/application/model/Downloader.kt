package io.eelo.appinstaller.application.model

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Constants
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import org.apache.commons.codec.binary.Hex
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.security.MessageDigest

class Downloader(private val applicationInfo: ApplicationInfo, private val fullData: FullData,
                 private val downloaderInterface: DownloaderInterface) {
    private lateinit var downloadManager: DownloadManager
    private lateinit var request: DownloadManager.Request

    private val listeners = ArrayList<(Int, Int) -> Unit>()
    private var totalBytes = 0
    private var downloadedBytes = 0

    private val notifier = ThreadedListeners {
        listeners.forEach {
            it.invoke(downloadedBytes, totalBytes) }
    }

    fun addListener(listener: (Int, Int) -> Unit) {
        listeners.add(listener)
    }

    fun download(context: Context) {
        downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        registerReceivers(context)
        if (fullData.getLastVersion() != null) {
            initialiseDownloadManagerRequest(context)
            handleDownloadUpdates(downloadManager.enqueue(request))
        } else {
            downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_FAILED)
        }
    }

    private fun registerReceivers(context: Context) {
        context.registerReceiver(onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun initialiseDownloadManagerRequest(context: Context) {
        request = DownloadManager.Request(
                Uri.parse(
                        Constants.DOWNLOAD_URL + fullData.getLastVersion()!!.downloadLink))
                .apply {
                    setTitle(fullData.basicData.name)
                    setDescription(context.getString(R.string.download_notification_description))
                    setDestinationInExternalFilesDir(
                            context,
                            Environment.DIRECTORY_DOWNLOADS,
                            applicationInfo.getApkFilename(fullData.basicData))
                }
    }

    private fun handleDownloadUpdates(downloadId: Long) {
        notifier.start()
        var isDownloading = true
        while (isDownloading) {
            val query = DownloadManager.Query().apply {
                setFilterById(downloadId)
            }
            val cursor = downloadManager.query(query)
            cursor.moveToFirst()
            downloadedBytes = cursor.getInt(
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            totalBytes = cursor.getInt(
                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL ||
                    downloadStatus == DownloadManager.STATUS_FAILED) {
                isDownloading = false
            }
        }
        notifier.stop()
    }

    @Throws(Exception::class)
    private fun getApkFileSha1(file: File): String {
        val messageDigest = MessageDigest.getInstance("SHA-1")
        val fileInputStream = FileInputStream(file)
        var length = 0
        val buffer = ByteArray(8192)
        while (length != -1) {
            length = fileInputStream.read(buffer)
            if (length > 0) {
                messageDigest.update(buffer, 0, length)
            }
        }
        return String(Hex.encodeHex(messageDigest.digest()))
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if (!fullData.getLastVersion()!!.apkSHA.isNullOrBlank() &&
                        fullData.getLastVersion()!!.apkSHA!! ==
                        getApkFileSha1(applicationInfo.getApkFile(context, fullData.basicData))) {
                    downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_SUCCESSFUL)
                    return
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
            downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_FAILED)
        }
    }
}
