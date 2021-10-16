package foundation.e.apps.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.fused.FusedAPIRepository
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository
) : ViewModel() {

    private val TAG = HomeViewModel::class.java.simpleName

    var homeScreenData: MutableLiveData<HomeScreen> = MutableLiveData()

    fun getHomeScreenData() {
        viewModelScope.launch {
            val data = fusedAPIRepository.getHomeScreenData()
            if (data.isSuccessful) {
                homeScreenData.postValue(data.body())
            }
        }
    }
}
