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
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
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
        private val reader = Common.getObjectMapper().readerFor(HomeResult::class.java)
    }

    fun request(callback: (Error?, HomeResult?) -> Unit) {
        try {
            var appType =mActivity.showApplicationTypePreference()
            val url = Constants.BASE_URL + "apps?action=list_home&source=$appType&type=$appType"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val result = reader.readValue<HomeResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class HomeResult @JsonCreator
    constructor(@JsonProperty("home") private val home: SubHomeResult) {

        fun getBannerApps(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, home.bannerApps)
        }

        fun getApps(applicationManager: ApplicationManager, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
            val apps = LinkedHashMap<Category, ArrayList<Application>>()
            for (pair in home.apps) {
                if(pair.value .isEmpty() ){
                    apps.remove(pair.key)
                }else {

                    apps[pair.key] = ApplicationParser.parseToApps(applicationManager, context, pair.value.toTypedArray())
                }
            }
            return apps
        }

    }

    class SubHomeResult @JsonCreator constructor() {
        val apps = LinkedHashMap<Category, ArrayList<BasicData>>()
        lateinit var bannerApps: Array<BasicData>

        @JsonAnySetter
        fun append(key: String, value: Any) {
            val apps = value as ArrayList<*>
            val appsData = ArrayList<BasicData>()
            apps.forEach {
                val data = it as LinkedHashMap<*, *>
                val appData = BasicData(
                        data["_id"] as String,
                        data["name"] as String,
                        data["package_name"] as String,
                        data["latest_version_number"].toString(),
                        data["latest_downloaded_version"].toString(),
                        data["x86_64_latest_downloaded_version"].toString(),
                        data["x86_64_latest_version_number"].toString(),
                        data["armeabi_latest_downloaded_version"].toString(),
                        data["armeabi_latest_version_number"].toString(),
                        data["arm64-v8a_latest_downloaded_version"].toString(),
                        data["arm64-v8a_latest_version_number"].toString(),
                        data["x86_latest_downloaded_version"].toString(),
                        data["x86_latest_version_number"].toString(),
                        data["armeabi-v7a_latest_downloaded_version"].toString(),
                        data["armeabi-v7a_latest_version_number"].toString(),
                        data["architectures"]as List<String> as ArrayList<String>,
                        data["author"] as String,
                        data["icon_image_path"] as String,
                        (data["other_images_path"] as List<String>).toTypedArray(),
                        data["exodus_score"].toString().toFloat(),
                        BasicData.Ratings(
                                (data["ratings"] as LinkedHashMap<String, Int>)
                                        ["usageQualityScore"]!!.toFloat(),
                                (data["ratings"] as LinkedHashMap<String, Int>)
                                        ["privacyScore"]!!.toFloat()),
                        data["category"] as String,
                        data["is_pwa"]as Boolean)
                appsData.add(appData)
            }
            if (key == "banner_apps") {
                bannerApps = appsData.toTypedArray()
            } else {
                this.apps[Category(key)] = appsData
            }
        }
    }
}
