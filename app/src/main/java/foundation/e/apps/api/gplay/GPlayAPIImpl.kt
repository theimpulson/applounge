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

package foundation.e.apps.api.gplay

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.Category
import com.aurora.gplayapi.data.models.File
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.AuthValidator
import com.aurora.gplayapi.helpers.CategoryHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import com.aurora.gplayapi.helpers.SearchHelper
import com.aurora.gplayapi.helpers.TopChartsHelper
import foundation.e.apps.api.gplay.token.TokenRepository
import foundation.e.apps.api.gplay.utils.GPlayHttpClient
import foundation.e.apps.utils.modules.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GPlayAPIImpl @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val dataStoreModule: DataStoreModule,
    private val gPlayHttpClient: GPlayHttpClient
) {

    // TODO: DON'T HARDCODE DISPATCHERS IN ANY METHODS
    suspend fun fetchAuthData() = withContext(Dispatchers.IO) {
        val data = async { tokenRepository.getAuthData() }
        data.await()?.let { dataStoreModule.saveCredentials(it) }
    }

    suspend fun fetchAuthData(email: String, aasToken: String) = withContext(Dispatchers.IO) {
        val data = async { tokenRepository.getAuthData(email, aasToken) }
        dataStoreModule.saveCredentials(data.await())
    }

    suspend fun validateAuthData(authData: AuthData): Boolean {
        var validity: Boolean
        withContext(Dispatchers.IO) {
            val authValidator = AuthValidator(authData).using(gPlayHttpClient)
            validity = authValidator.isValid()
        }
        return validity
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry> {
        val searchData = mutableListOf<SearchSuggestEntry>()
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(gPlayHttpClient)
            searchData.addAll(searchHelper.searchSuggestions(query))
        }
        return searchData.filter { it.suggestedQuery.isNotBlank() }
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<App> {
        val searchData = mutableListOf<App>()
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(gPlayHttpClient)
            val searchResult = searchHelper.searchResults(query)
            searchData.addAll(searchResult.appList)

            // Fetch more results in case the given result is a promoted app
            if (searchData.size == 1) {
                val searchBundle = searchHelper.next(searchResult.subBundles)
                searchData.addAll(searchBundle.appList)
            }
        }
        return searchData
    }

    suspend fun getDownloadInfo(
        packageName: String,
        versionCode: Int,
        offerType: Int,
        authData: AuthData
    ): List<File> {
        val downloadData = mutableListOf<File>()
        withContext(Dispatchers.IO) {
            val purchaseHelper = PurchaseHelper(authData).using(gPlayHttpClient)
            downloadData.addAll(purchaseHelper.purchase(packageName, versionCode, offerType))
        }
        return downloadData
    }

    suspend fun getAppDetails(packageName: String, authData: AuthData): App? {
        var appDetails: App?
        withContext(Dispatchers.IO) {
            val appDetailsHelper = AppDetailsHelper(authData).using(gPlayHttpClient)
            appDetails = appDetailsHelper.getAppByPackageName(packageName)
        }
        return appDetails
    }

    suspend fun getAppDetails(packageNameList: List<String>, authData: AuthData): List<App> {
        val appDetailsList = mutableListOf<App>()
        withContext(Dispatchers.IO) {
            val appDetailsHelper = AppDetailsHelper(authData).using(gPlayHttpClient)
            appDetailsList.addAll(appDetailsHelper.getAppByPackageName(packageNameList))
        }
        return appDetailsList
    }

    suspend fun getTopApps(
        type: TopChartsHelper.Type,
        chart: TopChartsHelper.Chart,
        authData: AuthData
    ): List<App> {
        val topApps = mutableListOf<App>()
        withContext(Dispatchers.IO) {
            val topChartsHelper = TopChartsHelper(authData).using(gPlayHttpClient)
            topApps.addAll(topChartsHelper.getCluster(type, chart).clusterAppList)
        }
        return topApps
    }

    suspend fun getCategoriesList(type: Category.Type, authData: AuthData): List<Category> {
        val categoryList = mutableListOf<Category>()
        withContext(Dispatchers.IO) {
            val categoryHelper = CategoryHelper(authData).using(gPlayHttpClient)
            categoryList.addAll(categoryHelper.getAllCategoriesList(type))
        }
        return categoryList
    }

    suspend fun listApps(browseUrl: String, authData: AuthData): List<App> {
        val list = mutableListOf<App>()
        withContext(Dispatchers.IO) {
            val categoryHelper = CategoryHelper(authData).using(gPlayHttpClient)
            val streamClusters = categoryHelper.getSubCategoryBundle(browseUrl).streamClusters
            // TODO: DEAL WITH DUPLICATE AND LESS ITEMS
            streamClusters.values.forEach {
                list.addAll(it.clusterAppList)
            }
        }
        return list
    }
}
