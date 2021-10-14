package foundation.e.apps.api.cleanapk

import foundation.e.apps.api.cleanapk.data.app.Application
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.download.Download
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.cleanapk.data.search.Search
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CleanAPKInterface {

    companion object {
        // API endpoints
        const val BASE_URL = "https://api.cleanapk.org"
        const val ASSET_URL = "https://api.cleanapk.org/v2/media/"

        // ACTIONS
        const val ACTION_SEARCH = "search"
        const val ACTION_LIST_APPS = "list_apps"
        const val ACTION_LIST_GAMES = "list_games"

        // Application sources
        const val APP_SOURCE_FOSS = "open"
        const val APP_SOURCE_ANY = "any"

        // Application types
        const val APP_TYPE_NATIVE = "native"
        const val APP_TYPE_PWA = "pwa"
        const val APP_TYPE_ANY = "any"
    }

    @GET("apps?action=list_home")
    suspend fun getHomeScreenData(
        @Query("type") type: String = APP_TYPE_ANY,
        @Query("source") source: String = APP_SOURCE_ANY,
    ): Response<HomeScreen>

    // TODO: Reminder that this function is for search App and PWA both
    @GET("/apps?action=app_detail")
    suspend fun getAppOrPWADetailsByID(
        @Query("id") id: String,
        @Query("architectures") architectures: List<String>? = null,
        @Query("type") type: String? = null
    ): Response<Application>

    // TODO: Reminder that action can be either "search", "list_apps" or "list_games"
    @GET("/apps")
    suspend fun searchOrListApps(
        @Query("keyword") keyword: String,
        @Query("action") action: String,
        @Query("source") source: String = APP_SOURCE_ANY,
        @Query("type") type: String = APP_TYPE_ANY,
        @Query("nres") nres: Int = 20,
        @Query("page") page: Int = 1,
        @Query("by") by: String? = null,
    ): Response<Search>

    @GET("/apps?action=download")
    suspend fun getDownloadInfo(
        @Query("app_id") id: String,
        @Query("version") version: String? = null,
        @Query("architecture") architecture: String? = null
    ): Response<Download>

    @GET("/apps?action=list_cat")
    suspend fun getCategoriesList(
        @Query("type") type: String = APP_TYPE_ANY,
        @Query("source") source: String = APP_SOURCE_ANY,
    ): Response<Categories>


}