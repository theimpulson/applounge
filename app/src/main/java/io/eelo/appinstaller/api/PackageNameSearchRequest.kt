package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class PackageNameSearchRequest(private val packageName: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun request(callback: (Error?, SearchResult?) -> Unit) {
        try {
            val url = URL(Constants.BASE_URL + "apps?action=search&keyword=${URLEncoder.encode(packageName, "utf-8")}&by=package_name")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = Constants.REQUEST_METHOD
            urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
            urlConnection.readTimeout = Constants.READ_TIMEOUT
            val result = reader.readValue<SearchResult>(urlConnection.inputStream)
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

    class SearchResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @param:JsonProperty("pages") val pages: Int,
                @param:JsonProperty("numberOfResults") val resultsNumber: Int,
                @param:JsonProperty("apps") val appResults: Array<BasicData>) {

        fun findOneAppData(packageName: String): BasicData? {
            appResults.forEach {
                if (it.packageName == packageName) {
                    return it
                }
            }
            return null
        }
    }
}