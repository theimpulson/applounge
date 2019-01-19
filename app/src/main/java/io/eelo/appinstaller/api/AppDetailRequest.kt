package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class AppDetailRequest(private val id: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(Result::class.java)
    }

    fun request(callback: (Error?, FullData?) -> Unit) {
        try {
            val url = URL(Constants.BASE_URL + "apps?action=app_detail&id=$id")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = Constants.REQUEST_METHOD
            urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
            urlConnection.readTimeout = Constants.READ_TIMEOUT
            val result = reader.readValue<Result>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result.app)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class Result @JsonCreator
    constructor(@JsonProperty("app") val app: FullData,
                @JsonProperty("success") private val success: Boolean)
}