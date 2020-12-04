package foundation.e.apps.utils

import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.application.model.data.FullData

object SystemAppDataSource {

    lateinit var basicData: BasicData

    fun createDataSource(id: String, tag: String, iconUri: String, downloadUrl: String): BasicData {
        basicData = BasicData(
                id = id,
                name = Constants.MICROG,
                packageName = Constants.MICROG_PACKAGE,
                lastVersionNumber = tag,
                lastVersionCode = 0,
                latestDownloadableUpdate = "",
                armeabi_latestDownloadableUpdate = "",
                arm64_v8a_latest_latestDownloadableUpdate = "",
                x86_latestDownloadableUpdate = "",
                armeabi_v7a_latestDownloadableUpdate = "",
                apkArchitecture = ArrayList(),
                author = "e-Foundation",
                iconUri = iconUri,
                imagesUri = arrayOf(),
                privacyRating = 0f,
                ratings = BasicData.Ratings(0f, 0f),
                category = Constants.SYSTEM_APPS,
                is_pwa = false,
                x86_64_latestDownloadableUpdate = "",
                downloadUrl = downloadUrl
        )
        return basicData
    }

    fun getFullData(): FullData {
        return FullData(
                basicData.id,
                basicData.name,
                basicData.packageName,
                basicData.lastVersionNumber,
                basicData.arm64_v8a_lastVersionCode,
                basicData.arm64_v8a_latest_latestDownloadableUpdate,
                basicData.x86_64_latestDownloadableUpdate,
                basicData.x86_64_lastVersionNumber,
                basicData.x86_64_lastVersionCode,
                basicData.armeabi_latestDownloadableUpdate,
                basicData.armeabi_lastVersionNumber,
                basicData.armeabi_lastVersionCode,
                basicData.arm64_v8a_latest_latestDownloadableUpdate,
                basicData.arm64_v8a_lastVersionNumber, basicData.arm64_v8a_lastVersionCode,
                basicData.x86_latestDownloadableUpdate,
                basicData.x86_lastVersionNumber,
                basicData.x86_lastVersionCode,
                basicData.armeabi_v7a_latestDownloadableUpdate,
                basicData.armeabi_v7a_lastVersionNumber,
                basicData.armeabi_v7a_lastVersionCode,
                basicData.apkArchitecture,
                basicData.author,
                basicData.iconUri,
                basicData.imagesUri, basicData.id, "", "", basicData.ratings, false,
                downloadUrl = basicData.downloadUrl
        )
    }

    fun basicData(): BasicData {
        return basicData
    }

}