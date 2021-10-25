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
