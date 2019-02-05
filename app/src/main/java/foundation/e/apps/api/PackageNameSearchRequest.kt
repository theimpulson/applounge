package foundation.e.apps.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import java.net.URLEncoder

class PackageNameSearchRequest(private val packageName: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun request(callback: (Error?, SearchResult?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=search&keyword=${URLEncoder.encode(packageName, "utf-8")}&by=package_name"
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