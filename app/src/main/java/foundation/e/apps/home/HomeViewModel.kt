package foundation.e.apps.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.gplay.OkHttpClient
import foundation.e.apps.api.gplay.token.TokenInterface
import foundation.e.apps.api.gplay.token.TokenRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val tokenRepository: TokenRepository
) : ViewModel() {

    // TODO: Get rid of code below

    fun getPlayDetails() {
        viewModelScope.launch(Dispatchers.IO) {
            val playResponse = OkHttpClient.postAuth(
                TokenInterface.BASE_URL,
                gson.toJson(properties).toByteArray()
            )
            val authData =
                async { gson.fromJson(String(playResponse.responseBytes), AuthData::class.java) }
            Log.d("HomeViewModel", gson.toJson(authData.await()))
        }
    }
}