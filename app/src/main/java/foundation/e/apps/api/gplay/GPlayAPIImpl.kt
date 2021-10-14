package foundation.e.apps.api.gplay

import com.aurora.gplayapi.SearchSuggestEntry
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

    companion object {
        var searchResult: List<SearchSuggestEntry>? = null
    }

    suspend fun fetchAuthData() = withContext(Dispatchers.IO) {
        val data = async { tokenRepository.getAuthData() }
        data.await()?.let { dataStoreModule.saveCredentials(it) }
    }

    // TODO: Fix the logic
    suspend fun getSearchSuggestions(query: String, authData: AuthData) =
        withContext(Dispatchers.IO) {
            val searchHelper = SearchHelper(authData).using(OkHttpClient)
            searchResult = searchHelper.searchSuggestions(query)
        }
}