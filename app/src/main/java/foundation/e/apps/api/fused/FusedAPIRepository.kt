package foundation.e.apps.api.fused

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.data.app.Application
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.download.Download
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.cleanapk.data.search.CleanAPKSearchApp
import foundation.e.apps.api.cleanapk.data.search.Search
import retrofit2.Response
import javax.inject.Inject

class FusedAPIRepository @Inject constructor(
    private val fusedAPIImpl: FusedAPIImpl
) {
    suspend fun getHomeScreenData(
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        source: String = CleanAPKInterface.APP_SOURCE_ANY
    ): Response<HomeScreen> {
        return fusedAPIImpl.getHomeScreenData(type, source)
    }

    suspend fun getAppOrPWADetailsByID(
        id: String,
        architectures: List<String>? = null,
        type: String? = null
    ): Response<Application> {
        return fusedAPIImpl.getAppOrPWADetailsByID(id, architectures, type)
    }

    suspend fun searchOrListApps(
        keyword: String,
        action: String,
        source: String = CleanAPKInterface.APP_SOURCE_FOSS,
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): Response<Search> {
        return fusedAPIImpl.searchOrListApps(keyword, action, source, type, nres, page, by)
    }

    suspend fun getDownloadInfo(
        id: String,
        version: String? = null,
        architecture: String? = null
    ): Response<Download> {
        return fusedAPIImpl.getDownloadInfo(id, version, architecture)
    }

    suspend fun getCategoriesList(
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        source: String = CleanAPKInterface.APP_SOURCE_ANY
    ): Response<Categories> {
        return fusedAPIImpl.getCategoriesList(type, source)
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry>? {
        return fusedAPIImpl.getSearchSuggestions(query, authData)
    }

    suspend fun fetchAuthData(): Unit? {
        return fusedAPIImpl.fetchAuthData()
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<CleanAPKSearchApp>? {
        return fusedAPIImpl.getSearchResults(query, authData)
    }
}