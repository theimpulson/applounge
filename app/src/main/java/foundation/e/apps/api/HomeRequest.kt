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
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.MainActivity.Companion.mActivity
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.categories.model.Category
import foundation.e.apps.utils.ApplicationParser
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class HomeRequest {

    companion object {
        private val mapper = Common.getObjectMapper()
    }

    fun request(callback: (Error?, Result?) -> Unit) {
        try {
            val appType = mActivity.showApplicationTypePreference()
            val url = Constants.BASE_URL + "apps?action=list_home&source=$appType&type=$appType"
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
            val bannerApps: List<BasicData>,
            @JsonProperty(TOP_UPDATED_APPS_KEY)
            val topUpdatedApps: List<BasicData>,
            @JsonProperty(TOP_UPDATED_GAMES_KEY)
            val topUpdatedGames: List<BasicData>,
            @JsonProperty(POPULAR_APPS_24_HOUR_KEY)
            val popularAppsIn24Hours: List<BasicData>,
            @JsonProperty(POPULAR_GAMES_24_HOUR_KEY)
            val popularGamesIn24Hours: List<BasicData>,
            @JsonProperty(DISCOVER_KEY)
            val discover: List<BasicData>
    ) {

        companion object {
            private const val BANNER_APPS_KEY = "banner_apps"
            private const val TOP_UPDATED_APPS_KEY = "top_updated_apps"
            private const val TOP_UPDATED_GAMES_KEY = "top_updated_games"
            private const val POPULAR_APPS_24_HOUR_KEY = "popular_apps_in_last_24_hours"
            private const val POPULAR_GAMES_24_HOUR_KEY = "popular_games_in_last_24_hours"
            private const val DISCOVER_KEY = "discover"
            private val KEYS = setOf(TOP_UPDATED_APPS_KEY,
                    TOP_UPDATED_GAMES_KEY, POPULAR_APPS_24_HOUR_KEY,
                    POPULAR_GAMES_24_HOUR_KEY, DISCOVER_KEY)
        }

        fun getBannerApps(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, bannerApps.toTypedArray())
        }

        fun getApps(applicationManager: ApplicationManager, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
            val apps = LinkedHashMap<Category, ArrayList<Application>>()
            KEYS.forEach {
                var heading = headings?.get(it)
                heading = heading
                        ?: "" // Use default heading as empty to let it generate from the key itself.
                val parsedApps = when (it) {
                    TOP_UPDATED_APPS_KEY -> ApplicationParser.parseToApps(applicationManager, context, topUpdatedApps.toTypedArray())
                    TOP_UPDATED_GAMES_KEY -> ApplicationParser.parseToApps(applicationManager, context, topUpdatedGames.toTypedArray())
                    POPULAR_APPS_24_HOUR_KEY -> ApplicationParser.parseToApps(applicationManager, context, popularAppsIn24Hours.toTypedArray())
                    POPULAR_GAMES_24_HOUR_KEY -> ApplicationParser.parseToApps(applicationManager, context, popularGamesIn24Hours.toTypedArray())
                    DISCOVER_KEY -> ApplicationParser.parseToApps(applicationManager, context, discover.toTypedArray())
                    else -> throw IllegalArgumentException("Unrecognised key $it encountered")
                }
                apps[Category(it, heading)] = parsedApps
            }
            return apps
        }
    }
}
