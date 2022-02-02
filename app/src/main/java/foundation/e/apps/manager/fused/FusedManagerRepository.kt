package foundation.e.apps.manager.fused

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import foundation.e.apps.manager.database.fused.FusedDownload
import foundation.e.apps.utils.enums.Status
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedManagerRepository @Inject constructor(
    private val fusedManagerImpl: FusedManagerImpl
) {

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        return fusedManagerImpl.createNotificationChannels()
    }

    suspend fun downloadAndInstallApp(fusedDownload: FusedDownload) {
        return fusedManagerImpl.downloadAndInstallApp(fusedDownload)
    }

    suspend fun addDownload(fusedDownload: FusedDownload) {
        return fusedManagerImpl.addDownload(fusedDownload)
    }

    fun getDownloadList(): LiveData<List<FusedDownload>> {
        return fusedManagerImpl.getDownloadList()
    }

    suspend fun updateDownloadStatus(downloadId: Long, status: Status) {
        return fusedManagerImpl.updateDownloadStatus(downloadId, status)
    }

    suspend fun updateDownloadStatus(packageName: String, status: Status) {
        return fusedManagerImpl.updateDownloadStatus(packageName, status)
    }

    suspend fun cancelDownload(downloadId: Long) {
        return fusedManagerImpl.cancelDownload(downloadId)
    }

    suspend fun cancelDownload(packageName: String) {
        return fusedManagerImpl.cancelDownload(packageName)
    }
}
