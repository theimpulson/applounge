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
import foundation.e.apps.MainActivity
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants

class AppDownloadedRequest(private val id: String) {
    companion object {
        private val reader = Common.getObjectMapper().readerFor(Result::class.java)
    }

    fun request() {
        try {
            val arch = System.getProperty("os.arch")
            val url = mActivity.BASE_URL() + "apps?action=download&app_id=$id&architecture=:$arch"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            reader.readValue<Result>(urlConnection.inputStream)
            urlConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    class Result @JsonCreator
    constructor()
}
