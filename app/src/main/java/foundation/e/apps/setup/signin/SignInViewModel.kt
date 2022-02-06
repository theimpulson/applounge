package foundation.e.apps.setup.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.gplay.utils.AC2DMTask
import foundation.e.apps.utils.enums.User
import foundation.e.apps.utils.modules.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val dataStoreModule: DataStoreModule,
    private val aC2DMTask: AC2DMTask,
    private val fusedAPIRepository: FusedAPIRepository
) : ViewModel() {

    val userType: LiveData<String> = dataStoreModule.userType.asLiveData()

    fun saveUserType(user: User) {
        viewModelScope.launch {
            dataStoreModule.saveUserType(user)
        }
    }

    fun fetchAuthData(email: String, oauthToken: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = aC2DMTask.getAC2DMResponse(email, oauthToken)
            response["Token"]?.let { fusedAPIRepository.fetchAuthData(email, it) }
        }
    }
}
