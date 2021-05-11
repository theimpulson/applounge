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

package foundation.e.apps.application.model.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Application
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class SearchAppsBasicData@JsonCreator
constructor(@param:JsonProperty("_id") val id: String,
            @param:JsonProperty("name") val name: String,
            @param:JsonProperty("package_name") val packageName: String?,
            @param:JsonProperty("latest_version_number") var lastVersionNumber: String,
            @param:JsonProperty("latest_version_code") var lastVersionCode: Long,
            @param:JsonProperty("latest_downloaded_version") val latestDownloadableUpdate: String,
            @param:JsonProperty("x86_64_latest_downloaded_version") val x86_64_latestDownloadableUpdate: String,
            @param:JsonProperty("x86_64_latest_version_number") val x86_64_lastVersionNumber: String = "-1",
            @param:JsonProperty("x86_64_latest_version_code") var x86_64_lastVersionCode: Long = -1,
            @param:JsonProperty("armeabi_latest_downloaded_version") val armeabi_latestDownloadableUpdate: String,
            @param:JsonProperty("armeabi_latest_version_number") val armeabi_lastVersionNumber: String = "-1",
            @param:JsonProperty("armeabi_latest_version_code") var armeabi_lastVersionCode: Long = -1,
            @param:JsonProperty("arm64-v8a_latest_downloaded_version") val arm64_v8a_latest_latestDownloadableUpdate: String,
            @param:JsonProperty("arm64-v8a_latest_version_number") val arm64_v8a_lastVersionNumber: String = "-1",
            @param:JsonProperty("arm64-v8a_latest_version_code") var arm64_v8a_lastVersionCode: Long = -1,
            @param:JsonProperty("x86_latest_downloaded_version") val x86_latestDownloadableUpdate: String,
            @param:JsonProperty("x86_latest_version_number") val x86_lastVersionNumber: String = "-1",
            @param:JsonProperty("x86_latest_version_code") var x86_lastVersionCode: Long = -1,
            @param:JsonProperty("armeabi-v7a_latest_downloaded_version") val armeabi_v7a_latestDownloadableUpdate: String,
            @param:JsonProperty("armeabi-v7a_latest_version_number") val armeabi_v7a_lastVersionNumber: String = "-1",
            @param:JsonProperty("armeabi-v7a_latest_version_code") var armeabi_v7a_lastVersionCode: Long = -1,
            @param:JsonProperty("architectures") val apkArchitecture: ArrayList<String>?,
            @param:JsonProperty("is_pwa") val is_pwa: Boolean,
            @param:JsonProperty("author") val author: String,
            @param:JsonProperty("is_web_app") val is_web_app: Boolean,
            @param:JsonProperty("ratings") val ratings: BasicData.Ratings,
            @param:JsonProperty("category") val category: String,
            @param:JsonProperty("icon_image_path") val icon_uri: String,
            @param:JsonProperty("other_images_path") val imagesUri: Array<String>){


    private var icon: Bitmap? = null
    private var images: List<Bitmap>? = null


    fun updateLoadedImages(other: SearchAppsBasicData) {
        if (icon_uri == other.icon_uri) {
            icon = other.icon
        }
        if (imagesUri.contentEquals(other.imagesUri)) {
            images = other.images
        }
    }

    fun loadIconAsync(application: Application, iconLoaderCallback: BasicData.IconLoaderCallback) {
        if (icon == null) {
            var error: Error? = null
            Execute({
                error = loadIconSynced()
            }, {
                if (error == null) {
                    icon?.let {
                        iconLoaderCallback.onIconLoaded(application, it)
                    }
                }
            })
        } else {
            iconLoaderCallback.onIconLoaded(application, icon!!)
        }
    }

    @Synchronized
    private fun loadIconSynced(): Error? {
        if (icon == null) {
            try {
                val url = URL(Constants.BASE_URL + "media/" + icon_uri)
                val urlConnection = url.openConnection() as HttpsURLConnection
                urlConnection.requestMethod = Constants.REQUEST_METHOD_GET
                urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
                urlConnection.readTimeout = Constants.READ_TIMEOUT
                icon = BitmapFactory.decodeStream(urlConnection.inputStream)
                urlConnection.disconnect()
            } catch (e: Exception) {
                e.printStackTrace()
                return Error.UNKNOWN

            }
        }
        return null
    }


    fun getLastVersion(): String? {
        if(apkArchitecture!=null) {

            //An ordered list of ABIs supported by this device. The most preferred ABI is the first element in the list.
            val arch = android.os.Build.SUPPORTED_ABIS.toList()
            when(arch[0]) {
                "arm64-v8a" -> {
                    return largestVersion(Pair(arm64_v8a_lastVersionNumber, arm64_v8a_lastVersionCode),
                            Pair(armeabi_v7a_lastVersionNumber, armeabi_v7a_lastVersionCode),
                            Pair(armeabi_lastVersionNumber, armeabi_lastVersionCode),
                            Pair(lastVersionNumber, lastVersionCode))
                }

                "armeabi-v7a" -> {
                    return largestVersion(
                            Pair(armeabi_v7a_lastVersionNumber, armeabi_v7a_lastVersionCode),
                            Pair(armeabi_lastVersionNumber, armeabi_lastVersionCode),
                            Pair(lastVersionNumber, lastVersionCode))
                }

                "armeabi" -> {
                    return largestVersion(
                            Pair(armeabi_lastVersionNumber, armeabi_lastVersionCode),
                            Pair(lastVersionNumber, lastVersionCode))
                }

                "x86-64" -> {
                    return largestVersion(
                            Pair(x86_64_lastVersionNumber, x86_64_lastVersionCode),
                            Pair(x86_lastVersionNumber, x86_lastVersionCode),
                            Pair(lastVersionNumber, lastVersionCode))
                }

                "x86" -> {
                    return largestVersion(Pair(x86_lastVersionNumber, x86_lastVersionCode),
                            Pair(lastVersionNumber, lastVersionCode))
                }
            }
        }

        return lastVersionNumber //universal
    }

    private fun largestVersion(vararg versions: Pair<String, Long>): String {
        var largestVersion = versions[0]
        versions.forEach {
            val version = it
            if (version.second > largestVersion.second) {
                largestVersion = version
            }
        }
        return largestVersion.first
    }
}