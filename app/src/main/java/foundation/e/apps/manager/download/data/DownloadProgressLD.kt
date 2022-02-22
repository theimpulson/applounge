package foundation.e.apps.manager.download.data

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DownloadProgressLD @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadManagerQuery: DownloadManager.Query,
) : LiveData<DownloadProgress>(), CoroutineScope {

    private val job = Job()
    private var downloadProgress = DownloadProgress()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onActive() {
        super.onActive()
        launch {
            while (isActive && downloadId.isNotEmpty()) {
                downloadManager.query(downloadManagerQuery.setFilterById(*downloadId.toLongArray()))
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

                            if (!downloadProgress.totalSizeBytes.containsKey(id) ||
                                downloadProgress.totalSizeBytes[id] != totalSizeBytes
                            ) {
                                downloadProgress.totalSizeBytes[id] = totalSizeBytes
                            }

                            if (!downloadProgress.bytesDownloadedSoFar.containsKey(id) ||
                                downloadProgress.bytesDownloadedSoFar[id] != bytesDownloadedSoFar
                            ) {
                                downloadProgress.bytesDownloadedSoFar[id] = bytesDownloadedSoFar
                            }

                            downloadProgress.status[id] =
                                status == DownloadManager.STATUS_SUCCESSFUL

                            postValue(downloadProgress)

                            if (downloadProgress.status.all { it.value }) {
                                clearDownload()
                                cancel()
                            }
                        }
                    }
                delay(500)
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        job.cancel()
    }

    companion object {
        private var downloadId = mutableListOf<Long>()

        fun setDownloadId(id: Long) {
            if (id == -1L) {
                clearDownload()
                return
            }
            downloadId.add(id)
        }

        private fun clearDownload() {
            downloadId.clear()
        }
    }
}
