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

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.MainActivity
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import java.net.URLEncoder

class PackageNameSearchRequest(private val packageName: String) {

    companion object {
        private val reader = Common.getObjectMapper().readerFor(SearchResult::class.java)
    }

    fun request(callback: (Error?, SearchResult?) -> Unit) {
        try {
            val url = mActivity.BASE_URL() + "apps?action=search&keyword=${URLEncoder.encode(packageName, "utf-8")}&by=package_name"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val result = reader.readValue<SearchResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class SearchResult @JsonCreator
    constructor(@param:JsonProperty("apps") val appResults: Array<BasicData>) {

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
