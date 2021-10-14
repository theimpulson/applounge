package foundation.e.apps.search

import androidx.lifecycle.*
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.data.search.CleanAPKSearchApp
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository,
    private val gson: Gson,
    dataStoreModule: DataStoreModule
) : ViewModel() {

    private val TAG = SearchViewModel::class.java.simpleName

    val authData: LiveData<String?> = dataStoreModule.authData.asLiveData()
    val searchSuggest: MutableLiveData<List<SearchSuggestEntry>?> = MutableLiveData()
    val searchResult: MutableLiveData<List<CleanAPKSearchApp>> = MutableLiveData()

    // TODO: GET RID OF AUTH DATA LOGIC COMPLETELY
    // Search function shouldn't care about authentication check, that's backend's job
    fun getAuthData() {
        viewModelScope.launch {
            fusedAPIRepository.fetchAuthData()
        }
    }

    fun getSearchSuggestions(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
            data?.let {
                searchSuggest.postValue(fusedAPIRepository.getSearchSuggestions(query, it))
            }
        }
    }

    // TODO: FIX THE CRAP CODING | DON'T SHIP IN PRODUCTION
    fun getSearchResults(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
            data?.let { it ->
                val response = mutableListOf<CleanAPKSearchApp>()
                val gplayResponse = fusedAPIRepository.getSearchResults(query, it)
                val cleanapkResponse =
                    fusedAPIRepository.searchOrListApps(
                        query,
                        CleanAPKInterface.ACTION_SEARCH,
                        CleanAPKInterface.APP_SOURCE_FOSS
                    ).body()

                cleanapkResponse?.let { response.addAll(it.apps) }
                gplayResponse?.let { response.addAll(it) }
                searchResult.postValue(response)
            }
        }
    }
}