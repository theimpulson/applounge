package foundation.e.apps.api.gplay.token

import android.util.Log
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import foundation.e.apps.api.gplay.OkHttpClient
import kotlinx.coroutines.async
import java.util.*
import javax.inject.Inject

@InstallIn(SingletonComponent::class)
class TokenImpl @Inject constructor(
    private val nativeDeviceProperty: Properties,
    private val gson: Gson
) {

    companion object {
        const val BASE_URL = "https://eu.gtoken.ecloud.global"
    }

    suspend fun getAuthData() {
        val playResponse =
            OkHttpClient.postAuth(BASE_URL, gson.toJson(nativeDeviceProperty).toByteArray())
        val authData =
            async { gson.fromJson(String(playResponse.responseBytes), AuthData::class.java) }
        Log.d("HomeViewModel", gson.toJson(authData.await()))
    }
}