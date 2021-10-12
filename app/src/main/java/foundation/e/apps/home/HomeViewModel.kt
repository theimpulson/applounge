package foundation.e.apps.home

import android.util.Log
import androidx.lifecycle.*
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.gplay.token.TokenRepository
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
    private val dataStoreModule: DataStoreModule
) : ViewModel() {

    private val TAG = HomeViewModel::class.java.simpleName
    val authData: LiveData<String?> = dataStoreModule.authData.asLiveData()

    // TODO: Get rid of code below

    fun getAuthData() {
        if (authData.value.isNullOrEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                val data = async { tokenRepository.getAuthData() }
                data.await()?.let { dataStoreModule.saveCredentials(it) }
            }
        }
    }


}