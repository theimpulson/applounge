package foundation.e.apps.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.cleanapk.CleanAPKInterface.Companion.ACTION_SEARCH
import foundation.e.apps.api.cleanapk.data.search.Search
import foundation.e.apps.api.fused.FusedAPIRepository
import kotlinx.coroutines.launch
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository
) : ViewModel() {

    // TODO: Get rid of code below
    val myResponse: MutableLiveData<Response<Search>> = MutableLiveData()

    fun searchApp() {
        viewModelScope.launch {
            myResponse.value = fusedAPIRepository.searchOrListApps("whatsapp", ACTION_SEARCH)
        }
    }
}