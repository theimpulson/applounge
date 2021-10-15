package foundation.e.apps.api.fused

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.cleanapk.data.app.Application
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.download.Download
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.data.SearchApp
import foundation.e.apps.api.data.Ratings
import foundation.e.apps.api.cleanapk.data.search.Search
import foundation.e.apps.api.data.Origin
import foundation.e.apps.api.gplay.GPlayAPIRepository
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FusedAPIImpl @Inject constructor(
    private val cleanAPKRepository: CleanAPKRepository,
    private val gPlayAPIRepository: GPlayAPIRepository
) {
    suspend fun getHomeScreenData(
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        source: String = CleanAPKInterface.APP_SOURCE_ANY
    ): Response<HomeScreen> {
        return cleanAPKRepository.getHomeScreenData(type, source)
    }

    suspend fun getAppOrPWADetailsByID(
        id: String,
        architectures: List<String>? = null,
        type: String? = null
    ): Response<Application> {
        return cleanAPKRepository.getAppOrPWADetailsByID(id, architectures, type)
    }

    suspend fun searchOrListApps(
        keyword: String,
        action: String,
        source: String = CleanAPKInterface.APP_SOURCE_FOSS,
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): List<SearchApp>? {
        val response = cleanAPKRepository.searchOrListApps(keyword, action, source, type, nres, page, by).body()

        // Gson does a really bad job of handling non-nullable values with default params, fix it
        response?.apps?.forEach {
            it.origin = Origin.CLEANAPK
        }
        return response?.apps
    }

    suspend fun getDownloadInfo(
        id: String,
        version: String? = null,
        architecture: String? = null
    ): Response<Download> {
        return cleanAPKRepository.getDownloadInfo(id, version, architecture)
    }

    suspend fun getCategoriesList(
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        source: String = CleanAPKInterface.APP_SOURCE_ANY
    ): Response<Categories> {
        return cleanAPKRepository.getCategoriesList(type, source)
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry>? {
        return gPlayAPIRepository.getSearchSuggestions(query, authData)
    }

    suspend fun fetchAuthData(): Unit? {
        return gPlayAPIRepository.fetchAuthData()
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<SearchApp>? {
        return gPlayAPIRepository.getSearchResults(query, authData)?.map { app ->
            app.transform()
        }
    }

    private fun App.transform(): SearchApp {
        return SearchApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            exodus_score = 0,
            icon_image_path = this.iconArtwork.url,
            name = this.displayName,
            package_name = this.packageName,
            ratings = Ratings(privacyScore = 0.0, usageQualityScore = this.labeledRating.toDouble()),
            origin = Origin.GPLAY
        )
    }
}