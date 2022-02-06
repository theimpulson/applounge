package foundation.e.apps.manager.fused

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import foundation.e.apps.manager.database.DatabaseRepository
import foundation.e.apps.manager.database.fused.FusedDownload
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.PWAManagerModule
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.Type
import kotlinx.coroutines.delay
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class FusedManagerImpl @Inject constructor(
    @Named("cacheDir") private val cacheDir: String,
    private val downloadManager: DownloadManager,
    private val notificationManager: NotificationManager,
    private val databaseRepository: DatabaseRepository,
    private val pwaManagerModule: PWAManagerModule,
    private val pkgManagerModule: PkgManagerModule,
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

    suspend fun getDownloadList(): List<FusedDownload> {
        return databaseRepository.getDownloadList()
    }

    fun getDownloadLiveList(): LiveData<List<FusedDownload>> {
        return databaseRepository.getDownloadLiveList()
    }

    suspend fun updateDownloadStatus(fusedDownload: FusedDownload, status: Status, downloadId: Long = 0) {
        if (fusedDownload.id.isNotBlank()) {
            if (downloadId != 0L && fusedDownload.downloadIdMap.containsKey(downloadId)) {
                fusedDownload.downloadIdMap[downloadId] = true
                databaseRepository.updateDownload(fusedDownload)
            }
            fusedDownload.status = status
            databaseRepository.updateDownload(fusedDownload)
        } else {
            Log.d(TAG, "Unable to update download status!")
        }
        if (status == Status.INSTALLED) flushOldDownload(fusedDownload.package_name)
    }

    suspend fun downloadApp(fusedDownload: FusedDownload) {
        when (fusedDownload.type) {
            Type.NATIVE -> downloadNativeApp(fusedDownload)
            Type.PWA -> pwaManagerModule.installPWAApp(fusedDownload)
        }
    }

    fun installApp(fusedDownload: FusedDownload) {
        val list = mutableListOf<File>()
        when (fusedDownload.type) {
            Type.NATIVE -> {
                val parentPathFile = File("$cacheDir/${fusedDownload.package_name}")
                parentPathFile.listFiles()?.let { list.addAll(it) }
                list.sort()
                if (list.size != 0) pkgManagerModule.installApplication(list)
            }
            else -> Log.d(TAG, "Unsupported application type!")
        }
    }

    suspend fun cancelDownload(fusedDownload: FusedDownload) {
        if (fusedDownload.id.isNotBlank()) {
            fusedDownload.downloadIdMap.forEach { (key, _) ->
                downloadManager.remove(key)
            }

            // Reset the status before deleting download
            updateDownloadStatus(fusedDownload, fusedDownload.orgStatus)
            delay(100)

            databaseRepository.deleteDownload(fusedDownload)
            flushOldDownload(fusedDownload.package_name)
        } else {
            Log.d(TAG, "Unable to cancel download!")
        }
    }

    suspend fun getFusedDownload(downloadId: Long = 0, packageName: String = ""): FusedDownload {
        val downloadList = getDownloadList()
        var fusedDownload = FusedDownload()
        downloadList.forEach {
            if (downloadId != 0L) {
                if (it.downloadIdMap.contains(downloadId)) {
                    fusedDownload = it
                }
            } else if (packageName.isNotBlank()) {
                if (it.package_name == packageName) {
                    fusedDownload = it
                }
            }
        }
        return fusedDownload
    }

    private fun flushOldDownload(packageName: String) {
        val parentPathFile = File("$cacheDir/$packageName")
        if (parentPathFile.exists()) parentPathFile.delete()
    }

    private suspend fun downloadNativeApp(fusedDownload: FusedDownload) {
        var count = 0
        fusedDownload.downloadURLList.forEach {
            count += 1
            val parentPath = "$cacheDir/${fusedDownload.package_name}"
            val packagePath = File(parentPath, "${fusedDownload.package_name}_$count.apk")

            // Clean old downloads and re-create download dir
            flushOldDownload(fusedDownload.package_name)
            File(parentPath).mkdir()

            val request = DownloadManager.Request(Uri.parse(it))
                .setTitle(if (count == 1) fusedDownload.name else "Additional file for ${fusedDownload.name}")
                .setDestinationUri(Uri.fromFile(packagePath))
            val requestId = downloadManager.enqueue(request)
            fusedDownload.apply {
                status = Status.DOWNLOADING
                downloadIdMap[requestId] = false
            }
            databaseRepository.updateDownload(fusedDownload)
        }
    }
}
