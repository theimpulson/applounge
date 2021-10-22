package foundation.e.apps.api.fused

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.cleanapk.data.app.Application
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.data.Origin
import foundation.e.apps.api.data.Ratings
import foundation.e.apps.api.data.SearchApp
import foundation.e.apps.api.gplay.GPlayAPIRepository
import foundation.e.apps.utils.PreferenceManagerModule
import foundation.e.apps.utils.pkg.PkgManagerModule
import retrofit2.Response
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FusedAPIImpl @Inject constructor(
    private val cleanAPKRepository: CleanAPKRepository,
    private val gPlayAPIRepository: GPlayAPIRepository,
    private val downloadManager: DownloadManager,
    private val pkgManagerModule: PkgManagerModule,
    private val preferenceManagerModule: PreferenceManagerModule,
    @ApplicationContext private val context: Context,
    @Named("cacheDir") private val cacheDir: String
) {
    private var TAG = FusedAPIImpl::class.java.simpleName

    suspend fun getHomeScreenData(
    ): Response<HomeScreen> {
        return when (preferenceManagerModule.preferredApplicationType()) {
            "open" -> {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_FOSS
                )
            }
            // TODO: Handle PWA response for home screen
            "pwa" -> {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_ANY
                )
            }
            else -> {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_ANY
                )
            }
        }
    }

    suspend fun getAppOrPWADetailsByID(
        id: String,
        architectures: List<String>? = null,
        type: String? = null
    ): Response<Application> {
        return cleanAPKRepository.getAppOrPWADetailsByID(id, architectures, type)
    }

    private suspend fun getCleanAPKSearchResults(
        keyword: String,
        action: String,
        source: String = CleanAPKInterface.APP_SOURCE_FOSS,
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): List<SearchApp>? {
        val response =
            cleanAPKRepository.searchOrListApps(keyword, action, source, type, nres, page, by)
                .body()

        // Gson does a really bad job of handling non-nullable values with default params, fix it
        response?.apps?.forEach {
            it.origin = Origin.CLEANAPK
        }
        return response?.apps
    }

    private suspend fun getCleanAPKDownloadInfo(
        id: String,
        version: String? = null,
        architecture: String? = null
    ): String? {
        return cleanAPKRepository.getDownloadInfo(id, version, architecture)
            .body()?.download_data?.download_link
    }

    private suspend fun getGplayDownloadInfo(
        packageName: String,
        versionCode: Int,
        offerType: Int,
        authData: AuthData
    ): String? {
        val response =
            gPlayAPIRepository.getDownloadInfo(packageName, versionCode, offerType, authData)
        return if (response != null) response[0].url else null
    }

    suspend fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int,
        authData: AuthData,
        origin: Origin
    ) {
        val downloadLink = if (origin == Origin.CLEANAPK) {
            getCleanAPKDownloadInfo(id)
        } else {
            getGplayDownloadInfo(packageName, versionCode, offerType, authData)
        }
        // Trigger the download
        if (downloadLink != null) {
            downloadApp(name, packageName, downloadLink)
        } else {
            Log.d(TAG, "Download link was null, exiting!")
        }
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

    private suspend fun getGplaySearchResults(query: String, authData: AuthData): List<SearchApp>? {
        return gPlayAPIRepository.getSearchResults(query, authData)?.map { app ->
            app.transform()
        }
    }

    /**
     * Fetches search results from cleanAPK and GPlay servers and returns them
     * @param query Query
     * @param authData [AuthData]
     * @return A list of nullable [SearchApp]
     */
    suspend fun getSearchResults(query: String, authData: AuthData): List<SearchApp> {
        val fusedResponse = mutableListOf<SearchApp>()
        var gplayResponse: List<SearchApp>? = null
        var cleanResponse: List<SearchApp>? = null

        when (preferenceManagerModule.preferredApplicationType()) {
            "any" -> {
                cleanResponse = getCleanAPKSearchResults(query, CleanAPKInterface.ACTION_SEARCH)
                gplayResponse = getGplaySearchResults(query, authData)
            }
            "open" -> {
                cleanResponse = getCleanAPKSearchResults(query, CleanAPKInterface.ACTION_SEARCH)
            }
            "pwa" -> {
                cleanResponse = getCleanAPKSearchResults(
                    query,
                    CleanAPKInterface.ACTION_SEARCH,
                    CleanAPKInterface.APP_SOURCE_ANY,
                    CleanAPKInterface.APP_TYPE_PWA
                )
            }
        }

        // Add all response together
        cleanResponse?.let { fusedResponse.addAll(it) }
        gplayResponse?.let { fusedResponse.addAll(it) }
        return fusedResponse
    }

    /**
     * Downloads the given package into the external cache directory
     * @param name Name of the package
     * @param packageName packageName of the package
     * @param url direct download link for the package
     */
    fun downloadApp(name: String, packageName: String, url: String) {
        val packagePath = File(cacheDir, "$packageName.apk")
        if (packagePath.exists()) packagePath.delete() // Delete old download if-exists
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setDestinationUri(Uri.fromFile(packagePath))
        downloadManager.enqueue(request)
    }

    /**
     * Installs an application from the given [Uri]
     * @param fileUri Uri of the file
     */
    fun installApp(fileUri: Uri) {
        val inputStream = context.contentResolver.openInputStream(fileUri)
        if (inputStream != null) {
            pkgManagerModule.installApplication(inputStream)
        } else {
            Log.d(TAG, "Input stream was null, exiting!")
        }
    }

    /**
     * Extension function to convert [App] into [SearchApp]
     */
    private fun App.transform(): SearchApp {
        return SearchApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            exodus_score = 0,
            icon_image_path = this.iconArtwork.url,
            name = this.displayName,
            package_name = this.packageName,
            ratings = Ratings(
                privacyScore = -1.0,
                usageQualityScore = if (this.labeledRating.isNotEmpty()) this.labeledRating.toDouble() else -1.0
            ),
            origin = Origin.GPLAY,
            latest_version_code = this.versionCode,
            offerType = this.offerType
        )
    }
}
