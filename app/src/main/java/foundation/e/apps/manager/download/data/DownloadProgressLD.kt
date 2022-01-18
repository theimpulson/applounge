package foundation.e.apps.manager.download.data

import android.app.DownloadManager
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class DownloadProgressLD @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadManagerQuery: DownloadManager.Query,
) : LiveData<DownloadProgress>(), CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job

    override fun onActive() {
        super.onActive()
        launch {
            while (isActive) {
                val cursor = downloadManager.query(downloadManagerQuery)
                while (cursor.moveToNext()) {
                    when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_SUCCESSFUL,
                        DownloadManager.STATUS_PENDING,
                        DownloadManager.STATUS_FAILED,
                        DownloadManager.STATUS_PAUSED -> {
                        }
                        else -> {
                            val downloadProgress = DownloadProgress(
                                cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)),
                                cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            )
                            postValue(downloadProgress)
                        }
                    }
                }
            }
        }
    }

    override fun onInactive() {
        super.onInactive()
        job.cancel()
    }
}
