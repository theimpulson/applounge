package foundation.e.apps.manager.workmanager

import android.app.DownloadManager
import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import foundation.e.apps.manager.database.DatabaseRepository
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.utils.enums.Status
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.takeWhile
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@HiltWorker
class InstallAppWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val params: WorkerParameters,
    private val databaseRepository: DatabaseRepository,
    private val fusedManagerRepository: FusedManagerRepository,
    private val downloadManager: DownloadManager,
    private val downloadManagerQuery: DownloadManager.Query,
) : CoroutineWorker(context, params) {

    private var isDownloading: Boolean = false

    companion object {
        private const val TAG = "InstallWorker"
        const val INPUT_DATA_FUSED_DOWNLOAD = "input_data_fused_download"
    }

    override suspend fun doWork(): Result {
        var fusedDownload: FusedDownload? = null
        try {
            val fusedDownloadString = params.inputData.getString(INPUT_DATA_FUSED_DOWNLOAD) ?: ""
            Log.d(TAG, "Fused download name $fusedDownloadString")

            fusedDownload = databaseRepository.getDownloadById(fusedDownloadString)
            fusedDownload?.let {
                if (fusedDownload.status != Status.AWAITING) {
                    return@let
                }
                startAppInstallationProcess(it)
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "doWork: Failed: ${e.stackTraceToString()}")
            fusedDownload?.let {
                fusedManagerRepository.installationIssue(it)
            }
            return Result.failure()
        }
    }

    private suspend fun startAppInstallationProcess(
        fusedDownload: FusedDownload
    ) {
        fusedManagerRepository.downloadApp(fusedDownload)
        Log.d(TAG, "===> doWork: Download started ${fusedDownload.name} ${fusedDownload.status}")
        isDownloading = true

        tickerFlow(3.seconds)
            .onEach {
                checkDownloadProcess(fusedDownload)
            }.launchIn(CoroutineScope(Dispatchers.Default))

        observeDownload(fusedDownload)
    }

    private fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (isDownloading) {
            emit(Unit)
            delay(period)
        }
    }

    private suspend fun checkDownloadProcess(fusedDownload: FusedDownload) {

        downloadManager.query(downloadManagerQuery.setFilterById(*fusedDownload.downloadIdMap.keys.toLongArray()))
            .use { cursor ->
                if (cursor.moveToFirst()) {
                    val id =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID))
                    val status =
                        cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
                    val totalSizeBytes =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                    val bytesDownloadedSoFar =
                        cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))

                    Log.d(
                        TAG,
                        "checkDownloadProcess: ${fusedDownload.name} $id $status $totalSizeBytes $bytesDownloadedSoFar"
                    )
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        isDownloading = false
                        if (status == DownloadManager.STATUS_FAILED) {
                            fusedManagerRepository.installationIssue(fusedDownload)
                        }
                    }
                }
            }
    }

    private suspend fun observeDownload(
        it: FusedDownload,
    ) {
        databaseRepository.getDownloadFlowById(it.id).takeWhile { isDownloading }
            .collect { fusedDownload ->
                if (fusedDownload == null) {
                    isDownloading = false
                    return@collect
                }
                Log.d(
                    TAG,
                    "doWork: flow collect ===> ${fusedDownload.name} ${fusedDownload.status}"
                )
                handleFusedDownloadStatus(fusedDownload)
            }
    }

    private fun handleFusedDownloadStatus(fusedDownload: FusedDownload) {
        when (fusedDownload.status) {
            Status.DOWNLOADING -> {
            }
            Status.INSTALLING -> {
                Log.d(
                    TAG,
                    "===> doWork: Installing ${fusedDownload.name} ${fusedDownload.status}"
                )
                isDownloading = false
            }
            Status.INSTALLED, Status.INSTALLATION_ISSUE -> {
                isDownloading = false
                Log.d(
                    TAG,
                    "===> doWork: Installed/Failed started ${fusedDownload.name} ${fusedDownload.status}"
                )
            }
            else -> {
                isDownloading = false
                Log.wtf(
                    TAG,
                    "===> ${fusedDownload.name} is in wrong state ${fusedDownload.status}"
                )
            }
        }
    }
}
