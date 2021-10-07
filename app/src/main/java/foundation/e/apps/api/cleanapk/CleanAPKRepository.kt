package foundation.e.apps.api.cleanapk

import foundation.e.apps.api.cleanapk.data.app.App
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.download.Download
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.cleanapk.data.search.Search
import retrofit2.Response
import javax.inject.Inject


class CleanAPKRepository @Inject constructor(
    private val cleanAPKInterface: CleanAPKInterface
) {

    suspend fun getHomeScreenData(
        type: String = "any",
        source: String = "any"
    ): Response<HomeScreen> {
        return cleanAPKInterface.getHomeScreenData(type, source)
    }

    suspend fun getAppOrPWADetailsByID(
        id: String,
        architectures: List<String>? = null,
        type: String? = null
    ): Response<App> {
        return cleanAPKInterface.getAppOrPWADetailsByID(id, architectures, type)
    }

    suspend fun searchOrListApps(
        keyword: String,
        action: String,
        source: String = "any",
        type: String = "any",
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): Response<Search> {
        return cleanAPKInterface.searchOrListApps(keyword, action, source, type, nres, page, by)
    }

    suspend fun getDownloadInfo(
        id: String,
        version: String? = null,
        architecture: String? = null
    ): Response<Download> {
        return cleanAPKInterface.getDownloadInfo(id, version, architecture)
    }

    suspend fun getCategoriesList(
        type: String = "any",
        source: String = "any"
    ): Response<Categories> {
        return cleanAPKInterface.getCategoriesList(type, source)
    }
}