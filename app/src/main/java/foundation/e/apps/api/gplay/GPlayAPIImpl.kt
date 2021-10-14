package foundation.e.apps.api.gplay

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
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

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry>? {
        var searchData: List<SearchSuggestEntry>?
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(OkHttpClient)
            searchData = searchHelper.searchSuggestions(query)
        }
        return searchData
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<App>? {
        var searchData: List<App>?
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(OkHttpClient)
            searchData = searchHelper.searchResults(query).appList
        }
        return searchData
    }
}