/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2022  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package foundation.e.apps.api

import android.app.DownloadManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
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

    fun downloadFileInCache(
        url: String,
        subDirectoryPath: String = "",
        fileName: String,
        downloadCompleted: ((Boolean, String) -> Unit)?
    ): Long {
        val directoryFile = File(cacheDir + subDirectoryPath)
        val downloadFile = File("$cacheDir/$fileName")
        if (!directoryFile.exists()) {
            directoryFile.mkdirs()
        }
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle("Downloading...")
            .setDestinationUri(Uri.fromFile(downloadFile))
        val downloadId = downloadManager.enqueue(request)
        isDownloading = true
        tickerFlow(.5.seconds).onEach {
            checkDownloadProgress(downloadId, downloadFile.absolutePath, downloadCompleted)
        }.launchIn(CoroutineScope(Dispatchers.IO))
        return downloadId
    }

    private fun checkDownloadProgress(
        downloadId: Long,
        filePath: String = "",
        downloadCompleted: ((Boolean, String) -> Unit)?
    ) {
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
                        "checkDownloadProcess: $filePath=> $bytesDownloadedSoFar/$totalSizeBytes $status"
                    )
                    if (status == DownloadManager.STATUS_FAILED) {
                        Log.d(
                            "DownloadManager",
                            "Download Failed: $filePath=> $bytesDownloadedSoFar/$totalSizeBytes $status"
                        )
                        isDownloading = false
                        downloadCompleted?.invoke(false, filePath)
                    } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Log.d(
                            "DownloadManager",
                            "Download Successful: $filePath=> $bytesDownloadedSoFar/$totalSizeBytes $status"
                        )
                        isDownloading = false
                        downloadCompleted?.invoke(true, filePath)
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
