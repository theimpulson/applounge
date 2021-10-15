package foundation.e.apps.managers.download

import android.util.Log
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import javax.inject.Inject
import javax.inject.Named

class DownloadManager @Inject constructor(
    private val fetchInstance: Fetch,
    @Named("cacheDir")
    private val cacheDir: String
) {

    // TODO: Implement download progress, completion, error and cancellation logic

    private val TAG = DownloadManager::class.java.simpleName

    /**
     * Generates a new request for download using [Fetch]
     * @param url URL
     * @param packageName Name of the package to download
     * @return an instance of [Request]
     */
    private fun request(url: String, packageName: String): Request {
        return Request(url, "$cacheDir/$packageName").apply {
            priority = Priority.HIGH
            networkType = NetworkType.ALL // TODO: Respect global network settings
        }
    }

    /**
     * Enqueues a new download using [Fetch]
     * @param url URL
     * @param packageName Name of the package to download
     * @return download id
     */
    fun enqueueDownload(url: String, packageName: String): Int {
        val request = request(url, packageName)
        fetchInstance.enqueue(request, {
            Log.d(TAG, "Download request placed for $packageName")
        }, {
            Log.e(TAG, it.toString())
        })
        return request.id
    }
}
