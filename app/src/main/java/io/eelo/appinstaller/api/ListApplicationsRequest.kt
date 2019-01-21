package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.Error
import java.net.URLEncoder

class ListApplicationsRequest(private val category: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = ObjectMapper().readerFor(ListApplicationsResult::class.java)
    }

    fun request(callback: (Error?, ListApplicationsResult?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=list_apps&category=${URLEncoder.encode(category, "utf-8")}&nres=$resultsPerPage&page=$page"
            val urlConnection = Common.createConnection(url)
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
                @JsonProperty("apps") private val apps: Array<BasicData>) {

        fun getApplications(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, apps)
        }
    }


}