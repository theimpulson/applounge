package foundation.e.apps.setup.tos

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TOSViewModel @Inject constructor(
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    val tocStatus: LiveData<Boolean> = dataStoreModule.tocStatus.asLiveData()
    val tocDate: LiveData<String> = dataStoreModule.tocDate.asLiveData()

    fun saveTOCStatus(status: Boolean) {
        viewModelScope.launch {
            dataStoreModule.saveTOCStatus(status)
        }
    }
}
