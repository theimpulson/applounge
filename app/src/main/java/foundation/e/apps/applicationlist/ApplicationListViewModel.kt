package foundation.e.apps.applicationlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.SearchApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository
) : ViewModel() {

    val list: MutableLiveData<List<SearchApp>> = MutableLiveData()

    fun getList(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            list.postValue(fusedAPIRepository.listApps(category))
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
