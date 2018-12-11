package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class ListApplicationsRequest(private val category: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = ObjectMapper().readerFor(ListApplicationsResult::class.java)
    }

    fun request(callback: (Error?, ListApplicationsResult?) -> Unit) {
        try {
            val url = URL(Constants.BASE_URL + "apps?action=list_apps&category=$category&nres=$resultsPerPage&page=$page")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = Constants.REQUEST_METHOD
            urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
            urlConnection.readTimeout = Constants.READ_TIMEOUT
            val result = reader.readValue<ListApplicationsResult>(urlConnection.inputStream)
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

    class ListApplicationsResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @JsonProperty("pages") val pages: Int,
                @JsonProperty("apps") private val apps: Array<FullData>) {

        fun getApplications(installManager: InstallManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(installManager, context, apps)
        }
    }


}