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
                @param:JsonProperty("latest_version_number") var lastVersionNumber: String?,
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
                @param:JsonProperty("is_pwa") val is_pwa: Boolean,
                @param:JsonProperty("author") val author: String?,
                @param:JsonProperty("is_web_app") val is_web_app: Boolean,
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