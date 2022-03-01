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

import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.utils.enums.Status
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DownloadManagerUtils @Inject constructor(
    private val fusedManagerRepository: FusedManagerRepository
) {
    private val TAG = DownloadManagerUtils::class.java.simpleName

    @DelicateCoroutinesApi
    fun cancelDownload(downloadId: Long) {
        GlobalScope.launch {
            val fusedDownload = fusedManagerRepository.getFusedDownload(downloadId)
            fusedManagerRepository.cancelDownload(fusedDownload)
        }
    }

    @DelicateCoroutinesApi
    fun updateDownloadStatus(downloadId: Long) {
        GlobalScope.launch {
            val fusedDownload = fusedManagerRepository.getFusedDownload(downloadId)
            delay(100)
            if (DownloadManagerBR.downloadedList.size == fusedDownload.downloadIdMap.size) {
                fusedManagerRepository.updateDownloadStatus(fusedDownload, Status.INSTALLING)
            }
        }
    }
}
