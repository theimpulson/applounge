/*
 * Copyright (C) 2022  ECORP
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

package foundation.e.apps.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.manager.database.DatabaseRepository
import foundation.e.apps.utils.enums.Status
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Illustration of how to get PWA installation status from broadcast from PWA player.
 * This class is not of much use here as after a PWA is installed, the FusedDownload instance
 * is deleted from the database.
 *
 * The sent intent contains following extras:
 * 1. SHORTCUT_ID - string shortcut id.
 * 2. URL - string url of the pwa.
 */
@AndroidEntryPoint
@DelicateCoroutinesApi
class PWAPlayerStatusReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PWA_ADDED = "foundation.e.pwaplayer.PWA_ADDED"
        const val ACTION_PWA_REMOVED = "foundation.e.pwaplayer.PWA_REMOVED"
    }

    @Inject
    lateinit var databaseRepository: DatabaseRepository

    override fun onReceive(context: Context?, intent: Intent?) {
        GlobalScope.launch {
            try {
                intent?.getStringExtra("SHORTCUT_ID")?.let { shortcutId ->
                    databaseRepository.getDownloadById(shortcutId)?.let { fusedDownload ->
                        when (intent.action) {
                            ACTION_PWA_ADDED -> {
                                fusedDownload.status = Status.INSTALLED
                                databaseRepository.updateDownload(fusedDownload)
                            }
                            ACTION_PWA_REMOVED -> {
                                databaseRepository.deleteDownload(fusedDownload)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
