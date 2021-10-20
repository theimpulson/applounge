package foundation.e.apps.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.data.Origin
import foundation.e.apps.api.data.SearchApp
import foundation.e.apps.api.fused.FusedAPIRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository,
) : ViewModel() {

    val searchSuggest: MutableLiveData<List<SearchSuggestEntry>?> = MutableLiveData()
    val searchResult: MutableLiveData<List<SearchApp>> = MutableLiveData()

    fun getSearchSuggestions(query: String, authData: AuthData) {
        viewModelScope.launch(Dispatchers.IO) {
            searchSuggest.postValue(fusedAPIRepository.getSearchSuggestions(query, authData))
        }
    }

    fun getSearchResults(query: String, authData: AuthData) {
        viewModelScope.launch(Dispatchers.IO) {
            searchResult.postValue(fusedAPIRepository.getSearchResults(query, authData))
        }
    }

    fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int,
        authData: AuthData,
        origin: Origin
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            fusedAPIRepository.getApplication(
                id,
                name,
                packageName,
                versionCode,
                offerType,
                authData,
                origin
            )
        }
    }
}
