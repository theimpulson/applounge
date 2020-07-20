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
import foundation.e.apps.application.model.data.FullData
import foundation.e.apps.application.model.data.PwaFullData
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class AppDetailRequest(private val id: String) {

    val sb = StringBuilder()

    companion object {
        private val reader = Common.getObjectMapper().readerFor(Result::class.java)
        private val Pwareader = Common.getObjectMapper().readerFor(PwaResult::class.java)
    }

    init {
        val arch = android.os.Build.SUPPORTED_ABIS.toList()
        var size = 0
        for (v in arch) {
            if (size < arch.size - 1) {
                sb.append("'$v',")
            } else {
                sb.append("'$v']")
            }
            size++
        }
    }

    fun request(callback: (Error?, FullData?) -> Unit) {
        try {
            val url = mActivity.BASE_URL() + "apps?action=app_detail&id=$id&architectures=[$sb"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val result = reader.readValue<Result>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result.app)


        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }


    fun Pwarequest(callback: (Error?, PwaFullData?) -> Unit) {
        try {
            val url = mActivity.BASE_URL() + "apps?action=app_detail&id=$id&architectures=[$sb"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val PwaResult = Pwareader.readValue<PwaResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, PwaResult.app)

        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class Result @JsonCreator
    constructor(@JsonProperty("app") val app: FullData)

    class PwaResult @JsonCreator
    constructor(@JsonProperty("app") val app: PwaFullData)
}


