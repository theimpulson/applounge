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

package foundation.e.apps.application.model.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import foundation.e.apps.application.model.Application
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Constants.BASE_URL
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute
import foundation.e.apps.utils.ImagesLoader
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class BasicData @JsonCreator
constructor(@param:JsonProperty("_id") val id: String,
            @param:JsonProperty("name") val name: String,
            @param:JsonProperty("package_name") val packageName: String,
            @param:JsonProperty("latest_version_number") var lastVersionNumber: String?,
            @param:JsonProperty("latest_downloaded_version") val latestDownloadableUpdate: String?,
            @param:JsonProperty("x86_64_latest_downloaded_version") val x86_64_latestDownloadableUpdate: String?,
            @param:JsonProperty("x86_64_latest_version_number") val x86_64_lastVersionNumber: String?,
            @param:JsonProperty("armeabi_latest_downloaded_version") val armeabi_latestDownloadableUpdate: String?,
            @param:JsonProperty("armeabi_latest_version_number") val armeabi_lastVersionNumber: String?,
            @param:JsonProperty("arm64-v8a_latest_downloaded_version") val arm64_v8a_latest_latestDownloadableUpdate: String?,
            @param:JsonProperty("arm64-v8a_latest_version_number") val arm64_v8a_lastVersionNumber: String?,
            @param:JsonProperty("x86_latest_downloaded_version") val x86_latestDownloadableUpdate: String?,
            @param:JsonProperty("x86_latest_version_number") val x86_lastVersionNumber: String?,
            @param:JsonProperty("armeabi-v7a_latest_downloaded_version") val armeabi_v7a_latestDownloadableUpdate: String?,
            @param:JsonProperty("armeabi-v7a_latest_version_number") val armeabi_v7a_lastVersionNumber: String?,
            @param:JsonProperty("architectures") val apkArchitecture: ArrayList<String>?,
            @param:JsonProperty("author") val author: String?,
            @param:JsonProperty("icon_image_path") private val iconUri: String,
            @param:JsonProperty("other_images_path") val imagesUri: Array<String>,
            @param:JsonProperty("exodus_score") val privacyRating: Float?,
            @param:JsonProperty("ratings") val ratings: Ratings?,
            @param:JsonProperty("category") val category: String,
            @param:JsonProperty("is_pwa") val is_pwa: Boolean){


    private var icon: Bitmap? = null
    private var images: List<Bitmap>? = null

    fun loadImagesAsyncly(getter: (List<Bitmap>) -> Unit) {
        if (images == null) {
                Execute({
                loadImagesSynced()
            }, {
                getter.invoke(images!!)
            })
        } else {
            getter.invoke(images!!)
        }
    }

    @Synchronized
    private fun loadImagesSynced() {
        if (images == null) {
            images = ImagesLoader(imagesUri).loadImages()
        }
    }

    fun loadIconAsync(application: Application, iconLoaderCallback: IconLoaderCallback) {
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
                val url = URL(BASE_URL + "media/" + iconUri)
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

    fun updateLoadedImages(other: BasicData) {
        if (iconUri == other.iconUri) {
            icon = other.icon
        }
        if (imagesUri.contentEquals(other.imagesUri)) {
            images = other.images
        }
    }

    class Ratings(@param:JsonProperty("usageQualityScore") val rating: Float?,
                  @param:JsonProperty("privacyScore") val privacyRating: Float?)

    interface IconLoaderCallback {
        fun onIconLoaded(application: Application, bitmap: Bitmap)
    }

    fun getLastVersion(): String? {
        if(apkArchitecture!=null) {

                //An ordered list of ABIs supported by this device. The most preferred ABI is the first element in the list.
                val arch = android.os.Build.SUPPORTED_ABIS.toList()
                when(arch[0]) {
                    "arm64-v8a" -> {
                        if (arm64_v8a_latest_latestDownloadableUpdate != "-1")
                            return arm64_v8a_lastVersionNumber // arm64 architecture

                        else if (armeabi_v7a_latestDownloadableUpdate != "-1")
                            return armeabi_v7a_lastVersionNumber //armeabi_v7a

                        else if (armeabi_latestDownloadableUpdate != "-1")
                            return armeabi_lastVersionNumber //armeabi

                        else lastVersionNumber
                    }

                    "armeabi-v7a" -> {
                        if (armeabi_v7a_latestDownloadableUpdate != "-1")
                            return armeabi_v7a_lastVersionNumber
                        else if (armeabi_latestDownloadableUpdate != "-1")
                            return armeabi_lastVersionNumber
                        else lastVersionNumber //universal
                    }

                    "armeabi" -> {
                        if (armeabi_latestDownloadableUpdate != "-1")
                            return armeabi_lastVersionNumber
                        else lastVersionNumber
                    }

                    "x86-64" -> {
                        if(x86_64_latestDownloadableUpdate != "-1")
                            return x86_64_lastVersionNumber
                        else if(x86_latestDownloadableUpdate != "-1")
                            return x86_lastVersionNumber
                        else lastVersionNumber
                    }

                    "x86" -> {
                        if(x86_latestDownloadableUpdate != "-1")
                            return x86_lastVersionNumber
                        else lastVersionNumber
                    }
                }
        }

        return lastVersionNumber //universal
    }
}
