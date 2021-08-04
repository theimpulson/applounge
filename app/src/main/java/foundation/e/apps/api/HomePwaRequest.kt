/*
 * Copyright (C) 2019-2021  E FOUNDATION
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

import android.content.Context
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.MainActivity
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.data.PwasBasicData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.ApplicationParser
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class HomePwaRequest {

    companion object {
        private val mapper = Common.getObjectMapper()
    }

    fun request(callback: (Error?, Result?) -> Unit) {
        try {
            var appType = MainActivity.mActivity.showApplicationTypePreference()
            val url = Constants.BASE_URL + "apps?action=list_home&type=$appType"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val result = mapper.readValue(urlConnection.inputStream, Result::class.java)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.invoke(Error.findError(e), null)
        }
    }

    data class Result(val success: Boolean, val home: Home)

    data class Home(
        @JsonProperty("headings")
        val headings: Map<String, String>?,
        @JsonProperty(BANNER_APPS_KEY)
        val bannerApps: List<PwasBasicData>,
        @JsonProperty(POPULAR_APPS_KEY)
        val topUpdatedApps: List<PwasBasicData>,
        @JsonProperty(POPULAR_GAMES_KEY)
        val topUpdatedGames: List<PwasBasicData>,
        @JsonProperty(DISCOVER_KEY)
        val discover: List<PwasBasicData>
    ) {

        companion object {
            private const val BANNER_APPS_KEY = "banner_apps"
            private const val POPULAR_APPS_KEY = "popular_apps"
            private const val POPULAR_GAMES_KEY = "popular_games"
            private const val DISCOVER_KEY = "discover"
            private val KEYS = setOf(
                POPULAR_APPS_KEY,
                POPULAR_GAMES_KEY, DISCOVER_KEY
            )
        }

        fun getBannerApps(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.PwaParseToApps(applicationManager, context, bannerApps.toTypedArray())
        }

        fun getApps(applicationManager: ApplicationManager, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
            val apps = LinkedHashMap<Category, ArrayList<Application>>()
            KEYS.forEach {
                var heading = headings?.get(it)
                heading = heading
                    ?: "" // Use default heading as empty to let it generate from the key itself.
                val parsedApps = when (it) {
                    POPULAR_APPS_KEY -> ApplicationParser.PwaParseToApps(applicationManager, context, topUpdatedApps.toTypedArray())
                    POPULAR_GAMES_KEY -> ApplicationParser.PwaParseToApps(applicationManager, context, topUpdatedGames.toTypedArray())
                    DISCOVER_KEY -> ApplicationParser.PwaParseToApps(applicationManager, context, discover.toTypedArray())
                    else -> throw IllegalArgumentException("Unrecognised key $it encountered")
                }
                apps[Category(it, heading)] = parsedApps
            }
            return apps
        }
    }
}
