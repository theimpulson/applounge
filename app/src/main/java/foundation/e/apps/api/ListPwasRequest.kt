package foundation.e.apps.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.ApplicationParser
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import java.net.URLEncoder

class ListPwasRequest(private val category: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = Common.getObjectMapper().readerFor(ListPwasResult::class.java)
    }

    fun request(callback: (Error?, ListPwasResult?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=list_apps&category=${URLEncoder.encode(category, "utf-8")}&nres=$resultsPerPage&page=$page&type=pwa"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val result = reader.readValue<ListPwasResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)


        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class ListPwasResult @JsonCreator
    constructor(@JsonProperty("apps") private val apps: Array<PwasBasicData>) {

        fun getApplications(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.PwaParseToApps(applicationManager, context, apps)
        }
    }
}