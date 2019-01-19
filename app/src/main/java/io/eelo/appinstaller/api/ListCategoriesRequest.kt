package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Common
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
            val url = Constants.BASE_URL + "apps?action=list_cat"
            val urlConnection = Common.createConnection(url)
            val result = reader.readValue<ListCategoriesResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class ListCategoriesResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @JsonProperty("apps") val appsCategories: Array<String>,
                @JsonProperty("games") val gamesCategories: Array<String>)

}