/*
 * Copyright (C) 2021  E FOUNDATION
 * Copyright (C) 2021  ECORP SAS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package foundation.e.apps.api

import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import java.util.*

class FDroidAppExistsRequest(private val keyword: String) {

    fun request(callback: (Error?, ArrayList<Int?>) -> Unit) {
        try {
            val l1 = ArrayList<Int?>()
            val url = Constants.F_DROID_PACKAGES_URL + keyword + "/"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val responseCode = urlConnection.responseCode

            urlConnection.disconnect()
            l1.add(responseCode)
            callback.invoke(null, l1)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), ArrayList())
        }
    }
}
