package foundation.e.apps.api.gplay.token

import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import foundation.e.apps.api.gplay.utils.OkHttpClient
import java.util.*
import javax.inject.Inject

class TokenImpl @Inject constructor(
    private val nativeDeviceProperty: Properties,
    private val gson: Gson,
) {

    companion object {
        const val BASE_URL = "https://eu.gtoken.ecloud.global"
    }

    fun getAuthData(): AuthData? {
        val playResponse =
            OkHttpClient.postAuth(BASE_URL, gson.toJson(nativeDeviceProperty).toByteArray())
        return gson.fromJson(String(playResponse.responseBytes), AuthData::class.java)
    }
}
