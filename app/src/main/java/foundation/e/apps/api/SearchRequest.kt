package foundation.e.apps.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.ApplicationParser
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import java.lang.Exception
import java.net.URLEncoder

class SearchRequest(private val keyword: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun request(callback: (Error?, SearchResult?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=search&keyword=${URLEncoder.encode(keyword, "utf-8")}&page=$page&nres=$resultsPerPage"
            val urlConnection = Common.createConnection(url)
            val result = reader.readValue<SearchResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class SearchResult @JsonCreator
    constructor(@JsonProperty("success") success: Boolean,
                @param:JsonProperty("pages") val pages: Int,
                @param:JsonProperty("numberOfResults") val resultsNumber: Int,
                @param:JsonProperty("apps") val appResults: Array<BasicData>) {

        fun getApplications(applicationManager: ApplicationManager, context: Context): List<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, appResults)
        }
    }
}