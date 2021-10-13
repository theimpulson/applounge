package foundation.e.apps.api.gplay

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.SearchHelper
import com.google.gson.Gson
import foundation.e.apps.api.gplay.token.TokenRepository
import foundation.e.apps.api.gplay.utils.OkHttpClient
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GPlayAPIImpl @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val gson: Gson,
    private val dataStoreModule: DataStoreModule
) {

    private val authData: LiveData<String?> = dataStoreModule.authData.asLiveData()
    var searchSuggestEntry: MutableLiveData<List<SearchSuggestEntry>> = MutableLiveData()

    suspend fun fetchAuthData() = withContext(Dispatchers.IO) {
        val data = async { tokenRepository.getAuthData() }
        data.await()?.let { dataStoreModule.saveCredentials(it) }
    }

    // TODO: Fix the logic
    fun getSearchSuggestions(query: String) {
        val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
        data?.let {
            val searchHelper = SearchHelper(it).using(OkHttpClient)
            searchSuggestEntry.postValue(searchHelper.searchSuggestions(query))
        }
    }
}