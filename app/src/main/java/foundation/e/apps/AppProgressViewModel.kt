package foundation.e.apps

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.download.data.DownloadProgress
import foundation.e.apps.manager.download.data.DownloadProgressLD
import foundation.e.apps.manager.fused.FusedManagerRepository
import javax.inject.Inject

@HiltViewModel
class AppProgressViewModel @Inject constructor(
    downloadProgressLD: DownloadProgressLD,
    private val fusedManagerRepository: FusedManagerRepository
) : ViewModel() {

    val downloadProgress = downloadProgressLD

    suspend fun calculateProgress(fusedApp: FusedApp?, progress: DownloadProgress): Pair<Long, Long> {
        fusedApp?.let { app ->
            val appDownload = fusedManagerRepository.getDownloadList()
                .singleOrNull { it.id.contentEquals(app._id) }
            val downloadingMap = progress.totalSizeBytes.filter { item ->
                appDownload?.downloadIdMap?.keys?.contains(item.key) == true
            }
            val totalSizeBytes = downloadingMap.values.sum()
            val downloadedSoFar = progress.bytesDownloadedSoFar.filter { item ->
                appDownload?.downloadIdMap?.keys?.contains(item.key) == true
            }.values.sum()

            return Pair(totalSizeBytes, downloadedSoFar)
        }
        return Pair(1, 0)
    }
}