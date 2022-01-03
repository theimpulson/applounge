package foundation.e.apps.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TOSViewModel @Inject constructor(
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    fun saveTOCStatus(status: Boolean) {
        viewModelScope.launch {
            dataStoreModule.saveTOCStatus(status)
        }
    }
}
