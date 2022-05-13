package foundation.e.apps.api.cleanapk.data.download

import android.app.DownloadManager
import android.net.Uri
import android.util.Log
import foundation.e.apps.manager.workmanager.InstallAppWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Singleton
class DownloadManager @Inject constructor(
    private val downloadManager: DownloadManager,
    @Named("cacheDir") private val cacheDir: String,
    private val downloadManagerQuery: DownloadManager.Query,
    ) {
    private var isDownloading = false

    fun downloadFile(url: String, fileName: String): Long {
        val directoryFile = File(cacheDir)
        val downloadFile = File("$cacheDir/$fileName")
        if(!directoryFile.exists()) {
            directoryFile.mkdirs()
        }
//        if(downloadFile.exists()) {
//            downloadFile.delete()
//        }
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading...")
            .setDestinationUri(Uri.fromFile(downloadFile))
        val downloadId = downloadManager.enqueue(request)
        isDownloading = true
        tickerFlow(1.seconds).onEach {
            checkDownloadProgress(downloadId)
        }
        return downloadId
    }

    private fun checkDownloadProgress(downloadId: Long, fileName: String = "") {
        downloadManager.query(downloadManagerQuery.setFilterById(downloadId))
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
                        "DownloadManager",
                        "checkDownloadProcess: $fileName=> $bytesDownloadedSoFar/$totalSizeBytes $status"
                    )
                    if (status == DownloadManager.STATUS_FAILED) {
                        Log.d(
                            "DownloadManager",
                            "Download Failed: $fileName=> $bytesDownloadedSoFar/$totalSizeBytes $status"
                        )
                    } else if(status == DownloadManager.STATUS_SUCCESSFUL) {
                        Log.d(
                            "DownloadManager",
                            "Download Successful: $fileName=> $bytesDownloadedSoFar/$totalSizeBytes $status"
                        )
                    }
                }
            }
    }

    private fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
        delay(initialDelay)
        while (isDownloading) {
            emit(Unit)
            delay(period)
        }
    }
}