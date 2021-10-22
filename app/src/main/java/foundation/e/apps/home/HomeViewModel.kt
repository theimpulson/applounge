package foundation.e.apps.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.Origin
import kotlinx.coroutines.Dispatchers
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
