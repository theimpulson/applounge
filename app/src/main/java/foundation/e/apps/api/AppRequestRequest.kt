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

import foundation.e.apps.MainActivity
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import org.json.JSONObject
import java.net.HttpURLConnection

class AppRequestRequest {

    fun request(packageName: String, callback: (Error?) -> Unit) {
        val requestBody = JSONObject()
        requestBody.put("package_name", packageName)
        try {
            val url = mActivity.BASE_URL() + "app_suggestions"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_POST)
            urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

            val outputStream = urlConnection.outputStream
            outputStream.write(requestBody.toString().toByteArray(charset("UTF-8")))
            outputStream.close()

            when (urlConnection.responseCode) {
                HttpURLConnection.HTTP_OK ->
                    callback.invoke(Error.NO_ERROR)
                HttpURLConnection.HTTP_BAD_REQUEST ->
                    callback.invoke(Error.INVALID_PACKAGE_NAME)
                HttpURLConnection.HTTP_NOT_ACCEPTABLE ->
                    callback.invoke(Error.PACKAGE_ALREADY_EXISTS)
                else ->
                    callback.invoke(Error.UNKNOWN)
            }

            urlConnection.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(Error.findError(e))
        }
    }
}
