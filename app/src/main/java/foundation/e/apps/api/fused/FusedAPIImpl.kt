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
import android.net.Uri
import android.util.Log
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.Category
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.FusedCategory
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Ratings
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.api.fused.utils.CategoryUtils
import foundation.e.apps.api.gplay.GPlayAPIRepository
import foundation.e.apps.utils.PreferenceManagerModule
import foundation.e.apps.utils.pkg.PkgManagerModule
import java.io.File
import java.util.UUID
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
    @Named("cacheDir") private val cacheDir: String
) {
    private var TAG = FusedAPIImpl::class.java.simpleName

    suspend fun getHomeScreenData(): HomeScreen? {
        val response = when (preferenceManagerModule.preferredApplicationType()) {
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
        }.body()
        return response
    }

    suspend fun getCategoriesList(type: Category.Type, authData: AuthData): List<FusedCategory> {
        val categoriesList = mutableListOf<FusedCategory>()
        val preferredApplicationType = preferenceManagerModule.preferredApplicationType()

        if (preferredApplicationType != "any") {
            val data = if (preferredApplicationType == "open") {
                cleanAPKRepository.getCategoriesList(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_FOSS
                ).body()
            } else {
                cleanAPKRepository.getCategoriesList(
                    CleanAPKInterface.APP_TYPE_PWA,
                    CleanAPKInterface.APP_SOURCE_ANY
                ).body()
            }
            data?.let { category ->
                when (type) {
                    Category.Type.APPLICATION -> {
                        for (cat in category.apps) {
                            val categoryApp = FusedCategory(
                                cat,
                                category.translations.getOrDefault(cat, ""),
                                "",
                                "",
                                CategoryUtils.provideCategoryIconResource(cat)
                            )
                            categoriesList.add(categoryApp)
                        }
                    }
                    Category.Type.GAME -> {
                        for (cat in category.games) {
                            val categoryApp = FusedCategory(
                                cat,
                                category.translations.getOrDefault(cat, ""),
                                "",
                                "",
                                CategoryUtils.provideCategoryIconResource(cat)
                            )
                            categoriesList.add(categoryApp)
                        }
                    }
                }
            }
        } else {
            val playResponse = gPlayAPIRepository.getCategoriesList(type, authData).map { app ->
                app.transformToFusedCategory()
            }
            categoriesList.addAll(playResponse)
        }
        return categoriesList
    }

    /**
     * Fetches search results from cleanAPK and GPlay servers and returns them
     * @param query Query
     * @param authData [AuthData]
     * @return A list of nullable [FusedApp]
     */
    suspend fun getSearchResults(query: String, authData: AuthData): List<FusedApp> {
        val fusedResponse = mutableListOf<FusedApp>()
        var gplayResponse: List<FusedApp>? = null
        var cleanResponse: List<FusedApp>? = null

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
        when (origin) {
            Origin.CLEANAPK -> {
                val downloadInfo = cleanAPKRepository.getDownloadInfo(id).body()
                val downloadLink = downloadInfo?.download_data?.download_link
                if (downloadLink != null) {
                    downloadApp(name, packageName, downloadLink)
                } else {
                    Log.d(TAG, "Download link was null, exiting!")
                }
            }
            Origin.GPLAY -> {
                val downloadList = gPlayAPIRepository.getDownloadInfo(
                    packageName,
                    versionCode,
                    offerType,
                    authData
                )
                // TODO: DEAL WITH MULTIPLE PACKAGES
                downloadApp(name, packageName, downloadList[0].url)
            }
            Origin.GITLAB -> {
            }
        }
    }

    suspend fun listApps(category: String, browseUrl: String, authData: AuthData): List<FusedApp>? {
        val preferredApplicationType = preferenceManagerModule.preferredApplicationType()

        if (preferredApplicationType != "any") {
            val response = if (preferredApplicationType == "open") {
                cleanAPKRepository.listApps(
                    category,
                    CleanAPKInterface.APP_SOURCE_FOSS,
                    CleanAPKInterface.APP_TYPE_ANY
                ).body()
            } else {
                cleanAPKRepository.listApps(
                    category,
                    CleanAPKInterface.APP_SOURCE_ANY,
                    CleanAPKInterface.APP_TYPE_PWA
                ).body()
            }
            response?.apps?.forEach {
                if (pkgManagerModule.isInstalled(it.package_name)) {
                    if (pkgManagerModule.isUpdatable(it.package_name, it.latest_version_code)) {
                        it.status = Status.UPDATABLE
                    } else {
                        it.status = Status.INSTALLED
                    }
                } else {
                    it.status = Status.UNAVAILABLE
                }
                it.origin = Origin.CLEANAPK
            }
            return response?.apps
        } else {
            return gPlayAPIRepository.listApps(browseUrl, authData).map { app ->
                app.transformToFusedApp()
            }
        }
    }

    suspend fun getApplicationDetails(
        id: String,
        packageName: String,
        authData: AuthData,
        origin: Origin
    ): FusedApp? {
        return if (origin == Origin.CLEANAPK) {
            val response = cleanAPKRepository.getAppOrPWADetailsByID(id).body()
            response?.let {
                if (pkgManagerModule.isInstalled(it.app.package_name)) {
                    if (pkgManagerModule.isUpdatable(
                            it.app.package_name,
                            it.app.latest_version_code
                        )
                    ) {
                        it.app.status = Status.UPDATABLE
                    } else {
                        it.app.status = Status.INSTALLED
                    }
                } else {
                    it.app.status = Status.UNAVAILABLE
                }
            }
            response?.app
        } else {
            val response = gPlayAPIRepository.getAppDetails(packageName, authData)
                ?.transformToFusedApp()
            response?.let {
                if (pkgManagerModule.isInstalled(it.package_name)) {
                    if (pkgManagerModule.isUpdatable(it.package_name, it.latest_version_code)) {
                        it.status = Status.UPDATABLE
                    } else {
                        it.status = Status.INSTALLED
                    }
                }
            }
            response
        }
    }

    private suspend fun getCleanAPKSearchResults(
        keyword: String,
        source: String = CleanAPKInterface.APP_SOURCE_FOSS,
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): List<FusedApp>? {
        val response =
            cleanAPKRepository.searchApps(keyword, source, type, nres, page, by).body()?.apps

        response?.forEach {
            if (pkgManagerModule.isInstalled(it.package_name)) {
                if (pkgManagerModule.isUpdatable(it.package_name, it.latest_version_code)) {
                    it.status = Status.UPDATABLE
                } else {
                    it.status = Status.INSTALLED
                }
            } else {
                it.status = Status.UNAVAILABLE
            }
            it.origin = Origin.CLEANAPK
        }
        return response
    }

    private suspend fun getGplaySearchResults(query: String, authData: AuthData): List<FusedApp> {
        val response = gPlayAPIRepository.getSearchResults(query, authData).map { app ->
            app.transformToFusedApp()
        }
        response.forEach {
            if (pkgManagerModule.isInstalled(it.package_name)) {
                if (pkgManagerModule.isUpdatable(it.package_name, it.latest_version_code)) {
                    it.status = Status.UPDATABLE
                } else {
                    it.status = Status.INSTALLED
                }
            }
        }
        return response
    }

    private fun downloadApp(name: String, packageName: String, url: String) {
        val packagePath = File(cacheDir, "$packageName.apk")
        if (packagePath.exists()) packagePath.delete() // Delete old download if-exists
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setDestinationUri(Uri.fromFile(packagePath))
        downloadManager.enqueue(request)
    }

    private fun App.transformToFusedApp(): FusedApp {
        return FusedApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            description = this.description,
            perms = this.permissions,
            trackers = emptyList(),
            icon_image_path = this.iconArtwork.url,
            last_modified = this.updatedOn,
            latest_version_code = this.versionCode,
            latest_version_number = this.versionName,
            licence = "",
            name = this.displayName,
            other_images_path = this.screenshots.transformToList(),
            package_name = this.packageName,
            ratings = Ratings(
                privacyScore = -1.0,
                usageQualityScore = if (this.labeledRating.isNotEmpty()) this.labeledRating.toDouble() else -1.0
            ),
            offer_type = this.offerType,
            status = Status.UNAVAILABLE,
            origin = Origin.GPLAY
        )
    }

    private fun MutableList<Artwork>.transformToList(): List<String> {
        val list = mutableListOf<String>()
        this.forEach {
            list.add(it.url)
        }
        return list
    }

    private fun Category.transformToFusedCategory(): FusedCategory {
        return FusedCategory(
            id = UUID.randomUUID().toString(),
            title = this.title,
            browseUrl = this.browseUrl,
            imageUrl = this.imageUrl,
            drawable = -1
        )
    }
}
