package foundation.e.apps.setup.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.utils.DataStoreModule
import foundation.e.apps.utils.USER
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    val userType: LiveData<String> = dataStoreModule.userType.asLiveData()

    fun saveUserType(user: USER) {
        viewModelScope.launch {
            dataStoreModule.saveUserType(user)
        }
    }
}
