package foundation.e.apps.search

import androidx.lifecycle.*
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.data.SearchApp
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

    private val authData: LiveData<String?> = dataStoreModule.authData.asLiveData()
    val searchSuggest: MutableLiveData<List<SearchSuggestEntry>?> = MutableLiveData()
    val searchResult: MutableLiveData<List<SearchApp>> = MutableLiveData()

    fun getSearchSuggestions(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
            data?.let {
                searchSuggest.postValue(fusedAPIRepository.getSearchSuggestions(query, it))
            }
        }
    }
    fun getSearchResults(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
            data?.let { it ->
                searchResult.postValue(fusedAPIRepository.getSearchResults(query, it))
            }
        }
    }
}
