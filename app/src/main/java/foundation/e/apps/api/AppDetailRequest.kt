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
import com.fasterxml.jackson.databind.ObjectMapper
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class AppDetailRequest(private val id: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(Result::class.java)
    }

    fun request(callback: (Error?, FullData?) -> Unit) {
        try {
            val url = Constants.BASE_URL + "apps?action=app_detail&id=$id"
            val urlConnection = Common.createConnection(url)
            val result = reader.readValue<Result>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result.app)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class Result @JsonCreator
    constructor(@JsonProperty("app") val app: FullData,
                @JsonProperty("success") private val success: Boolean)
}