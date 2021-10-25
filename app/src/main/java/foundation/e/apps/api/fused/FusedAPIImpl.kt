/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
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

package foundation.e.apps.api.fused

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.util.Log
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.cleanapk.data.app.Application
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.fused.data.CategoryApp
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Ratings
import foundation.e.apps.api.fused.data.SearchApp
import foundation.e.apps.api.gplay.GPlayAPIRepository
import foundation.e.apps.categories.model.Category
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

    suspend fun getHomeScreenData(): Response<HomeScreen> {
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

    suspend fun getCategoriesList(listType: String): List<CategoryApp> {
        val categoriesList = mutableListOf<CategoryApp>()
        val data = when (preferenceManagerModule.preferredApplicationType()) {
            "open" -> {
                cleanAPKRepository.getCategoriesList(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_FOSS
                ).body()
            }
            "pwa" -> {
                cleanAPKRepository.getCategoriesList(
                    CleanAPKInterface.APP_TYPE_PWA,
                    CleanAPKInterface.APP_SOURCE_ANY
                ).body()
            }
            else -> {
                cleanAPKRepository.getCategoriesList(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_ANY
                ).body()
            }
        }
        data?.let { category ->
            when (listType) {
                "apps" -> {
                    for (cat in category.apps) {
                        val categoryApp = CategoryApp(
                            cat,
                            category.translations.getOrDefault(cat, ""),
                            Category.provideCategoryIconResource(cat)
                        )
                        categoriesList.add(categoryApp)
                    }
                }
                "games" -> {
                    for (cat in category.games) {
                        val categoryApp = CategoryApp(
                            cat,
                            category.translations.getOrDefault(cat, ""),
                            Category.provideCategoryIconResource(cat)
                        )
                        categoriesList.add(categoryApp)
                    }
                }
            }
        }
        return categoriesList
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
                cleanResponse = getCleanAPKSearchResults(query)
                gplayResponse = getGplaySearchResults(query, authData)
            }
            "open" -> {
                cleanResponse = getCleanAPKSearchResults(query)
            }
            "pwa" -> {
                cleanResponse = getCleanAPKSearchResults(
                    query,
                    CleanAPKInterface.APP_SOURCE_ANY,
                    CleanAPKInterface.APP_TYPE_PWA
                )
            }
        }

        // Add all response together
        cleanResponse?.let { fusedResponse.addAll(it) }
        gplayResponse?.let { fusedResponse.addAll(it) }
        return fusedResponse.distinctBy { it.package_name }
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry> {
        return gPlayAPIRepository.getSearchSuggestions(query, authData)
    }

    suspend fun fetchAuthData(): Unit? {
        return gPlayAPIRepository.fetchAuthData()
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

    suspend fun listApps(category: String): List<SearchApp>? {
        val response = when (preferenceManagerModule.preferredApplicationType()) {
            "open" -> {
                cleanAPKRepository.listApps(
                    category,
                    CleanAPKInterface.APP_SOURCE_FOSS,
                    CleanAPKInterface.APP_TYPE_ANY
                )
            }
            "pwa" -> {
                cleanAPKRepository.listApps(
                    category,
                    CleanAPKInterface.APP_SOURCE_ANY,
                    CleanAPKInterface.APP_TYPE_PWA
                )
            }
            else -> {
                cleanAPKRepository.listApps(
                    category,
                    CleanAPKInterface.APP_SOURCE_ANY,
                    CleanAPKInterface.APP_TYPE_ANY
                )
            }
        }.body()
        // Gson does a really bad job of handling non-nullable values with default params, fix it
        response?.apps?.forEach {
            it.origin = Origin.CLEANAPK
        }
        return response?.apps
    }

    suspend fun getApplicationDetails(
        id: String,
        packageName: String,
        authData: AuthData,
        origin: Origin
    ): FusedApp? {
        return if (origin == Origin.CLEANAPK) {
            getCleanAPKAppDetails(id)?.app
        } else {
            getGPlayAppDetails(packageName, authData)
        }
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

    private suspend fun getCleanAPKAppDetails(
        id: String,
        architectures: List<String>? = null,
        type: String? = null
    ): Application? {
        return cleanAPKRepository.getAppOrPWADetailsByID(id, architectures, type).body()
    }

    private suspend fun getGPlayAppDetails(packageName: String, authData: AuthData): FusedApp? {
        return gPlayAPIRepository.getAppDetails(packageName, authData)?.transformToFusedApp()
    }

    private suspend fun getCleanAPKSearchResults(
        keyword: String,
        source: String = CleanAPKInterface.APP_SOURCE_FOSS,
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): List<SearchApp>? {
        val response =
            cleanAPKRepository.searchApps(keyword, source, type, nres, page, by)
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
    ): String {
        val response =
            gPlayAPIRepository.getDownloadInfo(packageName, versionCode, offerType, authData)
        return response[0].url
    }

    private suspend fun getGplaySearchResults(query: String, authData: AuthData): List<SearchApp> {
        return gPlayAPIRepository.getSearchResults(query, authData).map { app ->
            app.transformToSearchApp()
        }
    }

    /**
     * Downloads the given package into the external cache directory
     * @param name Name of the package
     * @param packageName packageName of the package
     * @param url direct download link for the package
     */
    private fun downloadApp(name: String, packageName: String, url: String) {
        val packagePath = File(cacheDir, "$packageName.apk")
        if (packagePath.exists()) packagePath.delete() // Delete old download if-exists
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setDestinationUri(Uri.fromFile(packagePath))
        downloadManager.enqueue(request)
    }

    private fun App.transformToSearchApp(): SearchApp {
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

    private fun App.transformToFusedApp(): FusedApp {
        return FusedApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            description = this.description,
            exodus_perms = emptyList(),
            exodus_tracker = emptyList(),
            icon_image_path = this.iconArtwork.url,
            last_modified = this.updatedOn,
            latest_version_code = this.versionCode,
            latest_version_number = this.versionName,
            licence = "",
            name = this.displayName,
            other_images_path = emptyList(),
            package_name = this.packageName,
            ratings = Ratings(
                privacyScore = -1.0,
                usageQualityScore = if (this.labeledRating.isNotEmpty()) this.labeledRating.toDouble() else -1.0
            )
        )
    }
}
