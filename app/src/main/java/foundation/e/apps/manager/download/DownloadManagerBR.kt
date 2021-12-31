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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.manager.pkg.PkgManagerModule
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class DownloadManagerBR : BroadcastReceiver() {

    @Inject
    lateinit var downloadManagerUtils: DownloadManagerUtils

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    private var TAG = DownloadManagerBR::class.java.simpleName

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            DownloadManager.ACTION_DOWNLOAD_COMPLETE -> {
                val id =
                    intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0)
                if (downloadManagerUtils.downloadSuccessful(id)) {
                    val file = File(downloadManagerUtils.downloadedFile(id))
                    if (file.exists()) {
                        pkgManagerModule.installApplication(file)
                    } else {
                        Log.d(TAG, "Given file doesn't exists, exiting!")
                    }
                } else {
                    Log.d(TAG, "Unable to get download id, exiting!")
                }
            }
            DownloadManager.ACTION_NOTIFICATION_CLICKED -> {
            }
        }
    }
}
