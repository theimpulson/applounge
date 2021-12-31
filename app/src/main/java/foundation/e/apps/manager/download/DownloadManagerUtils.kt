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
import javax.inject.Inject

class DownloadManagerUtils @Inject constructor(
    private val downloadManager: DownloadManager,
    private val downloadManagerQuery: DownloadManager.Query
) {
    private var EXTRA_DOWNLOAD_FAILED_ID: Long = 0

    fun downloadSuccessful(id: Long): Boolean {
        if (id == EXTRA_DOWNLOAD_FAILED_ID) return false
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        if (cursor.moveToFirst()) {
            val statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
            return if (statusIndex >= 0) cursor.getInt(statusIndex) == DownloadManager.STATUS_SUCCESSFUL else false
        }
        return false
    }

    fun downloadedFile(id: Long): String {
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        var fileUri = ""
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)
            fileUri = cursor.getString(index)
        }
        return fileUri.removePrefix("file://")
    }

    fun totalDownloadedBytes(id: Long): Long {
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        var size = ""
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
            size = cursor.getString(index)
        }
        return if (size.isNotBlank()) size.toLong() else 0
    }

    fun currentDownloadedBytes(id: Long): Long {
        val cursor = downloadManager.query(downloadManagerQuery.setFilterById(id))
        var size = ""
        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
            size = cursor.getString(index)
        }
        return if (size.isNotBlank()) size.toLong() else 0
    }
}
