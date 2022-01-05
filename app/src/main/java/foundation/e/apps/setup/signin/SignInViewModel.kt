package foundation.e.apps.setup.signin

import androidx.lifecycle.ViewModel
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

    fun saveUserType(user: USER) {
        viewModelScope.launch {
            dataStoreModule.saveUserType(user)
        }
    }
}
