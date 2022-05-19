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
import foundation.e.apps.manager.fused.FileManager
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
    @Named("cacheDir") private val cacheDir: String,
    @Named("ioCoroutineScope") private val coroutineScope: CoroutineScope
) {

    companion object {
        private const val APP_WARNING_LIST_FILE_URL =
            "https://gitlab.e.foundation/e/os/blocklist-app-lounge/-/raw/main/app-lounge-warning-list.json?inline=false"
        private const val WARNING_LIST_FILE_NAME = "app-lounge-warning-list.json"
    }

    private var blockedAppInfoList: AppWarningInfo? = null

    fun getBlockedAppPackages(): List<String> {
        return blockedAppInfoList?.notWorkingApps ?: listOf()
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
            val outputPath = "$cacheDir/warning_list/"
            FileManager.moveFile("$cacheDir/", WARNING_LIST_FILE_NAME, outputPath)
            val downloadedFile = File(outputPath + WARNING_LIST_FILE_NAME)
            val blockedAppInfoJson = String(downloadedFile.inputStream().readBytes())
            blockedAppInfoList = gson.fromJson(blockedAppInfoJson, AppWarningInfo::class.java)
        }
    }
}
