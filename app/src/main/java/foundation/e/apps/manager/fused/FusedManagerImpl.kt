package foundation.e.apps.manager.fused

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.manager.database.DatabaseRepository
import foundation.e.apps.manager.database.fused.FusedDownload
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class FusedManagerImpl @Inject constructor(
    @Named("cacheDir") private val cacheDir: String,
    private val downloadManager: DownloadManager,
    private val notificationManager: NotificationManager,
    private val databaseRepository: DatabaseRepository,
    @Named("download") private val downloadNotificationChannel: NotificationChannel,
    @Named("update") private val updateNotificationChannel: NotificationChannel
) {

    private val TAG = FusedManagerImpl::class.java.simpleName

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        notificationManager.apply {
            createNotificationChannel(downloadNotificationChannel)
            createNotificationChannel(updateNotificationChannel)
        }
    }

    suspend fun addDownload(fusedDownload: FusedDownload) {
        fusedDownload.status = Status.QUEUED
        databaseRepository.addDownload(fusedDownload)
    }

    fun getDownloadList(): LiveData<List<FusedDownload>> {
        return databaseRepository.getDownloadList()
    }

    suspend fun updateDownloadStatus(downloadId: Long, status: Status) {
        val fusedDownload = databaseRepository.getDownloadById(downloadId)
        if (fusedDownload.isNotEmpty() && fusedDownload.size == 1) {
            fusedDownload[0].status = status
            databaseRepository.updateDownload(fusedDownload[0])
        } else {
            Log.d(TAG, "Download ID mismatch!")
        }
        if (status == Status.INSTALLED) flushOldDownload(fusedDownload[0].package_name)
    }

    suspend fun updateDownloadStatus(packageName: String, status: Status) {
        val fusedDownload = databaseRepository.getDownloadByPkg(packageName)
        if (fusedDownload.isNotEmpty() && fusedDownload.size == 1) {
            fusedDownload[0].status = status
            databaseRepository.updateDownload(fusedDownload[0])
        } else {
            Log.d(TAG, "Package name mismatch!")
        }
        if (status == Status.INSTALLED) flushOldDownload(packageName)
    }

    suspend fun downloadAndInstallApp(fusedDownload: FusedDownload) {
        val packagePath = File(cacheDir, "${fusedDownload.package_name}.apk")
        if (packagePath.exists()) packagePath.delete() // Delete old download if-exists
        val request = DownloadManager.Request(Uri.parse(fusedDownload.downloadLink))
            .setTitle(fusedDownload.name)
            .setDestinationUri(Uri.fromFile(packagePath))
        val requestId = downloadManager.enqueue(request)
        fusedDownload.apply {
            status = Status.DOWNLOADING
            downloadId = requestId
        }
        databaseRepository.updateDownload(fusedDownload)
    }

    suspend fun cancelDownload(downloadId: Long) {
        val fusedDownload = databaseRepository.getDownloadById(downloadId)
        if (fusedDownload.isNotEmpty() && fusedDownload.size == 1) {
            if (downloadId != 0L) downloadManager.remove(downloadId)

            // Reset the status before deleting download
            updateDownloadStatus(downloadId, fusedDownload[0].orgStatus)
            delay(100)

            databaseRepository.deleteDownload(fusedDownload[0])
            flushOldDownload(fusedDownload[0].package_name)
        } else {
            Log.d(TAG, "Download ID mismatch!")
        }
    }

    suspend fun cancelDownload(packageName: String) {
        val fusedDownload = databaseRepository.getDownloadByPkg(packageName)
        if (fusedDownload.isNotEmpty() && fusedDownload.size == 1) {
            if (fusedDownload[0].downloadId != 0L) downloadManager.remove(fusedDownload[0].downloadId)

            // Reset the status before deleting download
            updateDownloadStatus(packageName, fusedDownload[0].orgStatus)
            delay(100)

            databaseRepository.deleteDownload(fusedDownload[0])
            flushOldDownload(fusedDownload[0].package_name)
        } else {
            Log.d(TAG, "Package name mismatch!")
        }
    }

    private fun flushOldDownload(packageName: String) {
        val packagePath = File(cacheDir, "$packageName.apk")
        if (packagePath.exists()) packagePath.delete()
    }
}
