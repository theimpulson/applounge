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
import com.aurora.gplayapi.helpers.TopChartsHelper
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.FusedCategory
import foundation.e.apps.api.fused.data.FusedHome
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Ratings
import foundation.e.apps.api.fused.utils.CategoryUtils
import foundation.e.apps.api.gplay.GPlayAPIRepository
import foundation.e.apps.utils.PreferenceManagerModule
import foundation.e.apps.utils.pkg.PkgManagerModule
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
    @Named("cacheDir") private val cacheDir: String
) {
    private var TAG = FusedAPIImpl::class.java.simpleName

    suspend fun getHomeScreenData(authData: AuthData): List<FusedHome> {
        val list = mutableListOf<FusedHome>()
        val preferredApplicationType = preferenceManagerModule.preferredApplicationType()
        val playList = listOf(
            TopChartsHelper.Chart.TOP_SELLING_FREE,
            TopChartsHelper.Chart.TOP_GROSSING,
            TopChartsHelper.Chart.MOVERS_SHAKERS,
        )

        if (preferredApplicationType != "any") {
            val response = if (preferredApplicationType == "open") {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_FOSS
                ).body()
            } else {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_ANY
                ).body()
            }
        } else {
            playList.forEach {
                val result = fetchTopAppsAndGames(it, authData)
                list.addAll(result)
            }
        }
        return list
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
                                id = cat,
                                title = category.translations.getOrDefault(cat, ""),
                                drawable = CategoryUtils.provideCategoryIconResource(cat)
                            )
                            categoriesList.add(categoryApp)
                        }
                    }
                    Category.Type.GAME -> {
                        for (cat in category.games) {
                            val categoryApp = FusedCategory(
                                id = cat,
                                title = category.translations.getOrDefault(cat, ""),
                                drawable = CategoryUtils.provideCategoryIconResource(cat)
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

        when (preferenceManagerModule.preferredApplicationType()) {
            "any" -> {
                fusedResponse.addAll(getCleanAPKSearchResults(query))
                fusedResponse.addAll(getGplaySearchResults(query, authData))
            }
            "open" -> {
                fusedResponse.addAll(getCleanAPKSearchResults(query))
            }
            "pwa" -> {
                fusedResponse.addAll(
                    getCleanAPKSearchResults(
                        query,
                        CleanAPKInterface.APP_SOURCE_ANY,
                        CleanAPKInterface.APP_TYPE_PWA
                    )
                )
            }
        }
        return fusedResponse
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
                it.status =
                    pkgManagerModule.getPackageStatus(it.package_name, it.latest_version_code)
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
        val response = if (origin == Origin.CLEANAPK) {
            cleanAPKRepository.getAppOrPWADetailsByID(id).body()?.app
        } else {
            gPlayAPIRepository.getAppDetails(packageName, authData)?.transformToFusedApp()
        }
        response?.let {
            it.status = pkgManagerModule.getPackageStatus(it.package_name, it.latest_version_code)
        }
        return response
    }

    private suspend fun getCleanAPKSearchResults(
        keyword: String,
        source: String = CleanAPKInterface.APP_SOURCE_FOSS,
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        nres: Int = 20,
        page: Int = 1,
        by: String? = null
    ): List<FusedApp> {
        val list = mutableListOf<FusedApp>()
        val response =
            cleanAPKRepository.searchApps(keyword, source, type, nres, page, by).body()?.apps

        response?.forEach {
            it.status = pkgManagerModule.getPackageStatus(it.package_name, it.latest_version_code)
            list.add(it)
        }
        return list
    }

    private suspend fun getGplaySearchResults(query: String, authData: AuthData): List<FusedApp> {
        return gPlayAPIRepository.getSearchResults(query, authData).map { app ->
            app.transformToFusedApp()
        }
    }

    private fun downloadApp(name: String, packageName: String, url: String) {
        val packagePath = File(cacheDir, "$packageName.apk")
        if (packagePath.exists()) packagePath.delete() // Delete old download if-exists
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setDestinationUri(Uri.fromFile(packagePath))
        downloadManager.enqueue(request)
    }

    private suspend fun fetchTopAppsAndGames(
        chart: TopChartsHelper.Chart,
        authData: AuthData
    ): List<FusedHome> {
        val list = mutableListOf<FusedHome>()
        val type = listOf(
            TopChartsHelper.Type.APPLICATION,
            TopChartsHelper.Type.GAME
        )
        type.forEach {
            val result = gPlayAPIRepository.getTopApps(it, chart, authData).map { app ->
                app.transformToFusedApp()
            }
            list.add(FusedHome("", result))
        }
        return list
    }

    private fun App.transformToFusedApp(): FusedApp {
        return FusedApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            description = this.description,
            perms = this.permissions.transformPermsToString(),
            icon_image_path = this.iconArtwork.url,
            last_modified = this.updatedOn,
            latest_version_code = this.versionCode,
            latest_version_number = this.versionName,
            name = this.displayName,
            other_images_path = this.screenshots.transformToList(),
            package_name = this.packageName,
            ratings = Ratings(
                usageQualityScore = if (this.labeledRating.isNotEmpty()) this.labeledRating.toDouble() else -1.0
            ),
            offer_type = this.offerType,
            status = pkgManagerModule.getPackageStatus(this.packageName, this.versionCode),
            origin = Origin.GPLAY
        )
    }

    private fun MutableList<String>.transformPermsToString(): String {
        val list = this.toString().replace(", ", "\n")
        return list.substring(1, list.length - 1)
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
            title = this.title,
            browseUrl = this.browseUrl,
            imageUrl = this.imageUrl,
        )
    }
}
