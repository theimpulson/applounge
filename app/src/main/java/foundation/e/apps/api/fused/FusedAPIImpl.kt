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
import android.text.format.Formatter
import android.util.Log
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.Artwork
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.Category
import com.aurora.gplayapi.helpers.TopChartsHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import foundation.e.apps.R
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.home.Home
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.FusedCategory
import foundation.e.apps.api.fused.data.FusedHome
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Ratings
import foundation.e.apps.api.fused.utils.CategoryUtils
import foundation.e.apps.api.gplay.GPlayAPIRepository
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.PreferenceManagerModule
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

    suspend fun getHomeScreenData(authData: AuthData): List<FusedHome> {
        val list = mutableListOf<FusedHome>()
        val preferredApplicationType = preferenceManagerModule.preferredApplicationType()

        if (preferredApplicationType != "any") {
            val response = if (preferredApplicationType == "open") {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_ANY,
                    CleanAPKInterface.APP_SOURCE_FOSS
                ).body()
            } else {
                cleanAPKRepository.getHomeScreenData(
                    CleanAPKInterface.APP_TYPE_PWA,
                    CleanAPKInterface.APP_SOURCE_ANY
                ).body()
            }
            response?.home?.let {
                list.addAll(generateCleanAPKHome(it, preferredApplicationType))
            }
        } else {
            list.addAll(fetchGPlayHome(authData))
        }
        return list
    }

    suspend fun getCategoriesList(type: Category.Type, authData: AuthData): List<FusedCategory> {
        val categoriesList = mutableListOf<FusedCategory>()
        val preferredApplicationType = preferenceManagerModule.preferredApplicationType()

        if (preferredApplicationType != "any") {
            val data = if (preferredApplicationType == "open") {
                getOpenSourceCategories()
            } else {
                getPWAsCategories()
            }

            data?.let { category ->
                categoriesList.addAll(
                    getFusedCategoryBasedOnCategoryType(
                        category,
                        type,
                        if (preferredApplicationType == "open") "Open Source" else "PWA"
                    )
                )
            }
        } else {
            var data = getOpenSourceCategories()
            data?.let {
                categoriesList.addAll(getFusedCategoryBasedOnCategoryType(it, type, "Open Source"))
            }
            data = getPWAsCategories()
            data?.let {
                categoriesList.addAll(getFusedCategoryBasedOnCategoryType(it, type, "PWA"))
            }
            val playResponse = gPlayAPIRepository.getCategoriesList(type, authData).map { app ->
                val category = app.transformToFusedCategory()
                updateCategoryDrawable(category, app)
                category
            }
            categoriesList.addAll(playResponse)
        }
        categoriesList.sortBy { item -> item.title.lowercase() }
        return categoriesList
    }

    private fun updateCategoryDrawable(
        category: FusedCategory,
        app: Category
    ) {
        category.drawable =
            if (app.type == Category.Type.APPLICATION) CategoryUtils.provideCategoryIconResource(
                getCategoryIconName(category)
            ) else CategoryUtils.provideGamesCategoryIconResource(
                getCategoryIconName(
                    category
                )
            )
    }

    private fun getCategoryIconName(category: FusedCategory): String {
        var categoryTitle = category.title
        if (categoryTitle.contains("&")) {
            categoryTitle = categoryTitle.replace("&", "and")
        }
        categoryTitle = categoryTitle.replace(' ', '_')
        return categoryTitle.lowercase()
    }

    private fun getFusedCategoryBasedOnCategoryType(
        categories: Categories,
        categoryType: Category.Type,
        tag: String
    ): List<FusedCategory> {
        return when (categoryType) {
            Category.Type.APPLICATION -> {
                getAppsCategoriesAsFusedCategory(categories, tag)
            }
            Category.Type.GAME -> {
                getGamesCategoriesAsFusedCategory(categories, tag)
            }
        }
    }

    private fun getAppsCategoriesAsFusedCategory(
        categories: Categories,
        tag: String
    ): List<FusedCategory> {
        return categories.apps.map { category ->
            createFusedCategoryFromCategory(category, categories, Category.Type.APPLICATION, tag)
        }
    }

    private fun getGamesCategoriesAsFusedCategory(
        categories: Categories,
        tag: String
    ): List<FusedCategory> {
        return categories.games.map { category ->
            createFusedCategoryFromCategory(category, categories, Category.Type.GAME, tag)
        }
    }

    private fun createFusedCategoryFromCategory(
        category: String,
        categories: Categories,
        appType: Category.Type,
        tag: String
    ) = FusedCategory(
        id = category,
        title = categories.translations.getOrDefault(category, ""),
        drawable = if (appType == Category.Type.APPLICATION) CategoryUtils.provideCategoryIconResource(
            category
        ) else CategoryUtils.provideGamesCategoryIconResource(category),
        tag = tag
    )

    private suspend fun getPWAsCategories() = cleanAPKRepository.getCategoriesList(
        CleanAPKInterface.APP_TYPE_PWA,
        CleanAPKInterface.APP_SOURCE_ANY
    ).body()

    private suspend fun getOpenSourceCategories() = cleanAPKRepository.getCategoriesList(
        CleanAPKInterface.APP_TYPE_ANY,
        CleanAPKInterface.APP_SOURCE_FOSS
    ).body()

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
        return fusedResponse.distinctBy { it.package_name }
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry> {
        return gPlayAPIRepository.getSearchSuggestions(query, authData)
    }

    suspend fun fetchAuthData(): Unit? {
        return gPlayAPIRepository.fetchAuthData()
    }

    suspend fun validateAuthData(authData: AuthData): Boolean {
        return gPlayAPIRepository.validateAuthData(authData)
    }

    suspend fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int,
        authData: AuthData,
        origin: Origin
    ): Long {
        when (origin) {
            Origin.CLEANAPK -> {
                val downloadInfo = cleanAPKRepository.getDownloadInfo(id).body()
                val downloadLink = downloadInfo?.download_data?.download_link
                if (downloadLink != null) {
                    return downloadApp(name, packageName, downloadLink)
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
                return downloadApp(name, packageName, downloadList[0].url)
            }
            Origin.GITLAB -> {
            }
        }
        return 0
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
        packageNameList: List<String>,
        authData: AuthData,
        origin: Origin
    ): List<FusedApp> {
        val list = mutableListOf<FusedApp>()
        val response = if (origin == Origin.CLEANAPK) {
            val pkgList = mutableListOf<FusedApp>()
            packageNameList.forEach {
                val result = cleanAPKRepository.searchApps(
                    keyword = it,
                    by = "package_name"
                ).body()
                if (result?.apps?.isNotEmpty() == true && result.numberOfResults == 1) {
                    pkgList.add(result.apps[0])
                }
            }
            pkgList
        } else {
            gPlayAPIRepository.getAppDetails(packageNameList, authData).map { app ->
                app.transformToFusedApp()
            }
        }
        response.forEach { fusedApp ->
            fusedApp.status = pkgManagerModule.getPackageStatus(
                fusedApp.package_name,
                fusedApp.latest_version_code
            )
            list.add(fusedApp)
        }
        return list
    }

    suspend fun getApplicationDetails(
        id: String,
        packageName: String,
        authData: AuthData,
        origin: Origin
    ): FusedApp {
        val response = if (origin == Origin.CLEANAPK) {
            cleanAPKRepository.getAppOrPWADetailsByID(id).body()?.app
        } else {
            val app = gPlayAPIRepository.getAppDetails(packageName, authData)
            app?.transformToFusedApp()
        }
        response?.let {
            it.status = pkgManagerModule.getPackageStatus(it.package_name, it.latest_version_code)
        }
        return response ?: FusedApp()
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

    private fun downloadApp(name: String, packageName: String, url: String): Long {
        val packagePath = File(cacheDir, "$packageName.apk")
        if (packagePath.exists()) packagePath.delete() // Delete old download if-exists
        val request = DownloadManager.Request(Uri.parse(url))
            .setTitle(name)
            .setDestinationUri(Uri.fromFile(packagePath))
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
        return downloadManager.enqueue(request)
    }

    private fun generateCleanAPKHome(home: Home, prefType: String): List<FusedHome> {
        val list = mutableListOf<FusedHome>()
        Log.d("Aayush", home.toString())
        val headings = if (prefType == "open") {
            mapOf(
                "top_updated_apps" to context.getString(R.string.top_updated_apps),
                "top_updated_games" to context.getString(R.string.top_updated_games),
                "popular_apps_in_last_24_hours" to context.getString(R.string.popular_apps_in_last_24_hours),
                "popular_games_in_last_24_hours" to context.getString(R.string.popular_games_in_last_24_hours),
                "discover" to context.getString(R.string.discover)
            )
        } else {
            mapOf(
                "popular_apps" to context.getString(R.string.popular_apps),
                "popular_games" to context.getString(R.string.popular_games),
                "discover" to context.getString(R.string.discover)
            )
        }
        headings.forEach { (key, value) ->
            when (key) {
                "top_updated_apps" -> {
                    if (home.top_updated_apps.isNotEmpty()) {
                        list.add(FusedHome(value, home.top_updated_apps))
                    }
                }
                "top_updated_games" -> {
                    if (home.top_updated_games.isNotEmpty()) {
                        list.add(FusedHome(value, home.top_updated_games))
                    }
                }
                "popular_apps" -> {
                    if (home.popular_apps.isNotEmpty()) {
                        list.add(FusedHome(value, home.popular_apps))
                    }
                }
                "popular_games" -> {
                    if (home.popular_games.isNotEmpty()) {
                        list.add(FusedHome(value, home.popular_games))
                    }
                }
                "popular_apps_in_last_24_hours" -> {
                    if (home.popular_apps_in_last_24_hours.isNotEmpty()) {
                        list.add(FusedHome(value, home.popular_apps_in_last_24_hours))
                    }
                }
                "popular_games_in_last_24_hours" -> {
                    if (home.popular_games_in_last_24_hours.isNotEmpty()) {
                        list.add(FusedHome(value, home.popular_games_in_last_24_hours))
                    }
                }
                "discover" -> {
                    if (home.discover.isNotEmpty()) {
                        list.add(FusedHome(value, home.discover))
                    }
                }
            }
        }
        return list
    }

    private suspend fun fetchGPlayHome(authData: AuthData): List<FusedHome> {
        val list = mutableListOf<FusedHome>()
        val homeElements = mutableMapOf(
            context.getString(R.string.topselling_free_apps) to mapOf(TopChartsHelper.Chart.TOP_SELLING_FREE to TopChartsHelper.Type.APPLICATION),
            context.getString(R.string.topselling_free_games) to mapOf(TopChartsHelper.Chart.TOP_SELLING_FREE to TopChartsHelper.Type.GAME),
            context.getString(R.string.topgrossing_apps) to mapOf(TopChartsHelper.Chart.TOP_GROSSING to TopChartsHelper.Type.APPLICATION),
            context.getString(R.string.topgrossing_games) to mapOf(TopChartsHelper.Chart.TOP_GROSSING to TopChartsHelper.Type.GAME),
            context.getString(R.string.movers_shakers_apps) to mapOf(TopChartsHelper.Chart.MOVERS_SHAKERS to TopChartsHelper.Type.APPLICATION),
            context.getString(R.string.movers_shakers_games) to mapOf(TopChartsHelper.Chart.MOVERS_SHAKERS to TopChartsHelper.Type.GAME),
        )
        homeElements.forEach {
            val chart = it.value.keys.iterator().next()
            val type = it.value.values.iterator().next()
            val result = gPlayAPIRepository.getTopApps(type, chart, authData).map { app ->
                app.transformToFusedApp()
            }
            list.add(FusedHome(it.key, result))
        }
        return list
    }

    private fun App.transformToFusedApp(): FusedApp {
        return FusedApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            description = this.description,
            perms = this.permissions,
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
            origin = Origin.GPLAY,
            shareUrl = this.shareUrl,
            appSize = this.size.toStringFileSize()
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
            title = this.title,
            browseUrl = this.browseUrl,
            imageUrl = this.imageUrl,
        )
    }

    private fun Long.toStringFileSize(): String {
        return Formatter.formatFileSize(context, this)
    }
}
