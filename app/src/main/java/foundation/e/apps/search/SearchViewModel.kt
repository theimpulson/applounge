package foundation.e.apps.search

import androidx.lifecycle.*
import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.App
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.SearchHelper
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.cleanapk.data.search.CleanAPKSearchApp
import foundation.e.apps.api.cleanapk.data.search.Ratings
import foundation.e.apps.api.data.Origin
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.gplay.GPlayAPIRepository
import foundation.e.apps.api.gplay.utils.OkHttpClient
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val gPlayAPIRepository: GPlayAPIRepository,
    private val fusedAPIRepository: FusedAPIRepository,
    private val gson: Gson,
    dataStoreModule: DataStoreModule
) : ViewModel() {

    private val TAG = SearchViewModel::class.java.simpleName

    val authData: LiveData<String?> = dataStoreModule.authData.asLiveData()
    val searchSuggest: MutableLiveData<List<SearchSuggestEntry>?> = MutableLiveData()
    val searchResult: MutableLiveData<List<CleanAPKSearchApp>> = MutableLiveData()

    fun getAuthData() {
        viewModelScope.launch {
            gPlayAPIRepository.fetchAuthData()
        }
    }

    // TODO: Move below stuff to gplayimpl class and use FusedAPI
    fun getSearchSuggestions(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
            data?.let {
                val searchHelper = SearchHelper(it).using(OkHttpClient)
                searchSuggest.postValue(searchHelper.searchSuggestions(query))
            }
//            data?.let {
//                gPlayAPIRepository.getSearchSuggestions(query, it)
//                withContext(Dispatchers.Main) {
//                    GPlayAPIImpl.searchResult?.let { string ->
//                        Log.d(TAG, string[0].suggestedQuery)
//                    }
//                }
//            }
        }
    }

    fun getSearchResults(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = authData.value?.let { gson.fromJson(it, AuthData::class.java) }
            data?.let { it ->
                val searchHelper = SearchHelper(it).using(OkHttpClient)
                searchResult.postValue(searchHelper.searchResults(query).appList.map { app ->
                    app.transform()
                })
            }
        }
    }

    // THE REAL TRANSFORMATION MAGIC
    private fun App.transform(): CleanAPKSearchApp {
        return CleanAPKSearchApp(
            _id = this.id.toString(),
            author = this.developerName,
            category = this.categoryName,
            exodus_score = 0,
            icon_image_path = this.iconArtwork.url,
            name = this.displayName,
            package_name = this.packageName,
            ratings = Ratings(privacyScore = 0, usageQualityScore = 0),
            origin = Origin.GPLAY
        )
    }

//    fun getSearchResults(query: String) {
//        viewModelScope.launch {
//            val response =
//                fusedAPIRepository.searchOrListApps(query, CleanAPKInterface.ACTION_SEARCH)
//            if (response.isSuccessful) {
//                searchResult.value = response.body()?.apps
//            }
//        }
//    }


}