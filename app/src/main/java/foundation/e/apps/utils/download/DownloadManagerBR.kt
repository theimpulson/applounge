package foundation.e.apps.utils.download

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.api.fused.FusedAPIImpl
import javax.inject.Inject

@AndroidEntryPoint
class DownloadManagerBR : BroadcastReceiver() {

    @Inject
    lateinit var downloadManager: DownloadManager

    @Inject
    lateinit var downloadManagerQuery: DownloadManager.Query

    @Inject
    lateinit var fusedAPIImpl: FusedAPIImpl

    private var TAG = DownloadManagerBR::class.java.simpleName
    private var EXTRA_DOWNLOAD_FAILED_ID: Long = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val id =
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, EXTRA_DOWNLOAD_FAILED_ID)
                if (downloadSuccessful(id)) {
                    val fileUri = downloadManager.getUriForDownloadedFile(id)
                    fusedAPIImpl.installApp(fileUri)
                } else {
                    Log.d(TAG, "Unable to get download id, exiting!")
                }
            }
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
            }
        }
    }

    private fun downloadSuccessful(id: Long): Boolean {
        if (id == EXTRA_DOWNLOAD_FAILED_ID) return false
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            return if (statusIndex >= 0) cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL else false
        }
        return false
    }
}
