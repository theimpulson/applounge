package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import javax.net.ssl.HttpsURLConnection

class ListApplicationsRequest(private val category: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = ObjectMapper().readerFor(ListApplicationsResult::class.java)
    }

    fun request(callback: (Error?, ListApplicationsResult?) -> Unit) {
        try {
            val url = URL(Constants.BASE_URL + "apps?action=list_apps&category=${URLEncoder.encode(category, "utf-8")}&nres=$resultsPerPage&page=$page")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = Constants.REQUEST_METHOD
            urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
            urlConnection.readTimeout = Constants.READ_TIMEOUT
            val result = reader.readValue<ListApplicationsResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class ListApplicationsResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @JsonProperty("pages") val pages: Int,
                @JsonProperty("apps") private val apps: Array<FullData>) {

        fun getApplications(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, apps)
        }
    }


}