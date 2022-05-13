package foundation.e.apps.api

import foundation.e.apps.api.cleanapk.data.download.DownloadManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlockedAppRepository @Inject constructor(private val downloadManager: DownloadManager) {

    companion object {
        const val APP_WARNING_LIST_FILE_URL = "https://gitlab.e.foundation/e/os/blocklist-app-lounge/-/raw/main/app-lounge-warning-list.json?inline=false"
    }

    fun getBlockedAppPackages(): List<String> {
        return listOf()
    }

    fun fetchUpdateOfAppWarningList() {
        downloadManager.downloadFile(APP_WARNING_LIST_FILE_URL, "app-lounge-warning-list.json")
    }
}