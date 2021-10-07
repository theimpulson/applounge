package foundation.e.apps.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.cleanapk.CleanAPKInterface.Companion.ACTION_SEARCH
import foundation.e.apps.api.cleanapk.CleanAPKRepository
import foundation.e.apps.api.cleanapk.data.app.App
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.cleanapk.data.search.Search
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
// TODO: Remove the cleanAPK repo injection in favour of central API
class HomeViewModel @Inject constructor(
    private val cleanAPKRepository: CleanAPKRepository
) : ViewModel() {

    // TODO: Get rid of code below
    val myResponse: MutableLiveData<Response<Search>> = MutableLiveData()

    fun searchApp() {
        viewModelScope.launch {
            myResponse.value = cleanAPKRepository.searchOrListApps("whatsapp", ACTION_SEARCH)
        }
    }
}