package foundation.e.apps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository,
    dataStoreModule: DataStoreModule
) : ViewModel() {

    // Authentication Data for GPlay servers
    val authData: LiveData<String?> = dataStoreModule.authData.asLiveData()

    fun getAuthData() {
        viewModelScope.launch {
            fusedAPIRepository.fetchAuthData()
        }
    }

    fun downloadApp(name: String, packageName: String, url: String) {
        fusedAPIRepository.downloadApp(name, packageName, url)
    }
}
