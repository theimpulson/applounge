package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ListCategoriesRequest {

    companion object {
        private val reader = ObjectMapper().readerFor(ListCategoriesResult::class.java)
    }

    fun request(callback: (Error?, ListCategoriesResult?) -> Unit) {
        try {
            val url = URL(Constants.BASE_URL + "apps?action=list_cat")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = Constants.REQUEST_METHOD
            urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
            urlConnection.readTimeout = Constants.READ_TIMEOUT
            val result = reader.readValue<ListCategoriesResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: SocketTimeoutException) {
            callback.invoke(Error.REQUEST_TIMEOUT, null)
        } catch (e: IOException) {
            callback.invoke(Error.SERVER_UNAVAILABLE, null)
        } catch (e: Exception) {
            callback.invoke(Error.UNKNOWN, null)
        }
    }

    class ListCategoriesResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @JsonProperty("apps") val appsCategories: Array<String>,
                @JsonProperty("games") val gamesCategories: Array<String>)

}