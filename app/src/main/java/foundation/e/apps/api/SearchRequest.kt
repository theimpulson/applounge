/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
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

    class SearchResult @JsonCreator @JsonIgnoreProperties(ignoreUnknown = true)
    constructor(@JsonProperty("success") success: Boolean,
                @param:JsonProperty("pages") val pages: Int,
                @param:JsonProperty("numberOfResults") val resultsNumber: Int,
                @param:JsonProperty("apps") val appResults: Array<BasicData>) {

        fun getApplications(applicationManager: ApplicationManager, context: Context): List<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, appResults)
        }
    }
}
