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

package foundation.e.apps.manager.pkg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.manager.fused.FusedManagerRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
@DelicateCoroutinesApi
open class PkgManagerBR : BroadcastReceiver() {

    @Inject
    lateinit var fusedManagerRepository: FusedManagerRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (context != null && action != null) {
            val packageUid = intent.getIntExtra(Intent.EXTRA_UID, 0)
            val isUpdating = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)
            val packages = context.packageManager.getPackagesForUid(packageUid)

            packages?.let { pkgList ->
                pkgList.forEach { pkgName ->
                    when (action) {
                        Intent.ACTION_PACKAGE_ADDED -> updateDownloadStatus(pkgName)
                        Intent.ACTION_PACKAGE_REMOVED -> {
                            if (!isUpdating) deleteDownload(pkgName)
                        }
                    }
                }
            }
        }
    }

    // TODO: FIND A BETTER WAY TO DO THIS
    private fun updateDownloadStatus(packageName: String) {
        GlobalScope.launch {
            fusedManagerRepository.updateDownloadStatus(packageName, Status.INSTALLED)
        }
    }

    private fun deleteDownload(packageName: String) {
        GlobalScope.launch {
            fusedManagerRepository.cancelDownload(packageName)
        }
    }
}
