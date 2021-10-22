package foundation.e.apps.api.gplay

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.data.models.File
import com.aurora.gplayapi.helpers.AppDetailsHelper
import com.aurora.gplayapi.helpers.PurchaseHelper
import com.aurora.gplayapi.helpers.SearchHelper
import foundation.e.apps.api.gplay.token.TokenRepository
import foundation.e.apps.api.gplay.utils.OkHttpClient
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GPlayAPIImpl @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val dataStoreModule: DataStoreModule
) {

    suspend fun fetchAuthData() = withContext(Dispatchers.IO) {
        val data = async { tokenRepository.getAuthData() }
        data.await()?.let { dataStoreModule.saveCredentials(it) }
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry> {
        val searchData = mutableListOf<SearchSuggestEntry>()
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(OkHttpClient)
            searchData.addAll(searchHelper.searchSuggestions(query))
        }
        return searchData.filter { it.suggestedQuery.isNotBlank() }
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<App> {
        val searchData = mutableListOf<App>()
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(OkHttpClient)
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
            val purchaseHelper = PurchaseHelper(authData).using(OkHttpClient)
            downloadData.addAll(purchaseHelper.purchase(packageName, versionCode, offerType))
        }
        return downloadData
    }

    suspend fun getAppDetails(packageName: String, authData: AuthData): App? {
        var appDetails: App?
        withContext(Dispatchers.IO) {
            val appDetailsHelper = AppDetailsHelper(authData).using(OkHttpClient)
            appDetails = appDetailsHelper.getAppByPackageName(packageName)
        }
        return appDetails
    }
}
