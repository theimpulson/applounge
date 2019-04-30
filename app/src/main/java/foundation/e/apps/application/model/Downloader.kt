/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application.model

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import foundation.e.apps.R
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.utils.Constants
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.AsyncTask

class Downloader(private val applicationInfo: ApplicationInfo, private val fullData: FullData,
                 private val downloaderInterface: DownloaderInterface) :
        IntegrityVerificationCallback {
    private lateinit var downloadManager: DownloadManager
    private lateinit var request: DownloadManager.Request
    private var downloadId: Long = 0

    private val listeners = ArrayList<DownloadProgressCallback>()
    private var totalBytes = 0
    private var downloadedBytes = 0

    private val notifier = ThreadedListeners {
        listeners.forEach {
            it.notifyDownloadProgress(downloadedBytes, totalBytes)
        }
    }

    fun addListener(listener: DownloadProgressCallback) {
        listeners.add(listener)
    }

    fun removeListener(listener: DownloadProgressCallback) {
        listeners.remove(listener)
    }

    fun download(context: Context) {
        if (fullData.getLastVersion() != null) {
            downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            registerReceivers(context)
            initialiseDownloadManagerRequest(context)
            downloadId = downloadManager.enqueue(request)
            Thread {
                handleDownloadUpdates()
            }.start()
        } else {
            downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_FAILED)
        }
    }

    private fun registerReceivers(context: Context) {
        context.registerReceiver(onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
    }

    private fun unregisterReceivers(context: Context) {
        context.unregisterReceiver(onComplete)
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

    private fun handleDownloadUpdates() {
        notifier.start()
        while (true) {
            val query = DownloadManager.Query().apply {
                setFilterById(downloadId)
            }
            val cursor = downloadManager.query(query)
            if (!cursor.moveToFirst()) {
                break
            }

            downloadedBytes = cursor.getInt(
                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
            totalBytes = cursor.getInt(
                    cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))

            val downloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
            cursor.close()
            if (downloadStatus == DownloadManager.STATUS_SUCCESSFUL ||
                    downloadStatus == DownloadManager.STATUS_FAILED) {
                break
            }
        }
        notifier.stop()
    }

    fun cancelDownload() {
        downloadManager.remove(downloadId)
    }

    private var onComplete: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            unregisterReceivers(context)
            val status = getDownloadStatus()
            if (status != null && status == DownloadManager.STATUS_SUCCESSFUL) {
                IntegrityVerificationTask(
                        applicationInfo,
                        fullData,
                        this@Downloader)
                        .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context)
            } else {
                downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_FAILED)
            }
        }
    }

    override fun onIntegrityVerified(context: Context, verificationSuccessful: Boolean) {
        if (verificationSuccessful) {
            downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_SUCCESSFUL)
        } else {
            downloaderInterface.onDownloadComplete(context, DownloadManager.STATUS_FAILED)
        }
    }

    private fun getDownloadStatus(): Int? {
        val query = DownloadManager.Query().apply {
            setFilterById(downloadId)
        }
        val cursor = downloadManager.query(query)
        if (cursor.moveToNext()) {
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            val status = cursor.getInt(columnIndex)
            cursor.close()
            return status
        }
        return null
    }

    interface DownloadProgressCallback {
        fun notifyDownloadProgress(count: Int, total: Int)
    }
}
