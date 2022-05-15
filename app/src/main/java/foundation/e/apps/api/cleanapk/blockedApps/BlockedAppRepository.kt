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
package foundation.e.apps.api.cleanapk.blockedApps

import com.google.gson.Gson
import foundation.e.apps.api.DownloadManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class BlockedAppRepository @Inject constructor(
    private val downloadManager: DownloadManager,
    private val gson: Gson,
    @Named("ioCoroutineScope") private val coroutineScope: CoroutineScope
) {

    companion object {
        const val APP_WARNING_LIST_FILE_URL =
            "https://gitlab.e.foundation/e/os/blocklist-app-lounge/-/raw/main/app-lounge-warning-list.json?inline=false"
    }

    private var blockedAppInfoList: AppWarningInfo? = null

    fun getBlockedAppPackages(): List<String> {
        return blockedAppInfoList?.not_working_apps ?: listOf()
    }

    fun fetchUpdateOfAppWarningList() {
        downloadManager.downloadFileInCache(
            APP_WARNING_LIST_FILE_URL,
            fileName = "app-lounge-warning-list.json"
        ) { success, path ->
            if (success) {
                parseBlockedAppDataFromFile(path)
            }
        }
    }

    private fun parseBlockedAppDataFromFile(path: String) {
        coroutineScope.launch {
            val downloadedFile = File(path)
            val blockedAppInfoJson = String(downloadedFile.inputStream().readBytes())
            blockedAppInfoList = gson.fromJson(blockedAppInfoJson, AppWarningInfo::class.java)
        }
    }
}
