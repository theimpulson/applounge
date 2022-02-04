/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
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

package foundation.e.apps.manager.download

import android.app.DownloadManager
import android.util.Log
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.utils.enums.Status
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class DownloadManagerUtils @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadManagerQuery: DownloadManager.Query,
    private val fusedManagerRepository: FusedManagerRepository
) {
    private val TAG = DownloadManagerUtils::class.java.simpleName

    fun downloadStatus(id: Long): Int {
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS)
            return cursor.getInt(statusIndex)
        }
        return DownloadManager.STATUS_FAILED
    }

    @DelicateCoroutinesApi
    fun checkAndUpdateStatus(downloadId: Long) {
        val file = File(downloadedFile(downloadId))
        if (file.exists()) {
            updateDownloadStatus(downloadId)
        } else {
            Log.d(TAG, "Given file doesn't exists, exiting!")
        }
    }

    @DelicateCoroutinesApi
    fun cancelDownload(downloadId: Long) {
        GlobalScope.launch {
            val fusedDownload = fusedManagerRepository.getFusedDownload(downloadId)
            fusedManagerRepository.cancelDownload(fusedDownload)
        }
    }

    private fun downloadedFile(id: Long): String {
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        var fileUri = ""
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI)
            fileUri = cursor.getString(index)
        }
        return fileUri.removePrefix("file://")
    }

    @DelicateCoroutinesApi
    private fun updateDownloadStatus(downloadId: Long) {
        GlobalScope.launch {
            delay(100)
            val fusedDownload = fusedManagerRepository.getFusedDownload(downloadId)
            fusedManagerRepository.updateDownloadStatus(fusedDownload, Status.INSTALLING, downloadId)
        }
    }
}
