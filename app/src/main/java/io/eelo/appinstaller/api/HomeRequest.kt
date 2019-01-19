package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.categories.model.Category
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.lang.Exception
import java.net.SocketTimeoutException
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class HomeRequest {

    companion object {
        private val reader = ObjectMapper().readerFor(HomeResult::class.java)
    }

    fun request(callback: (Error?, HomeResult?) -> Unit) {
        try {
            val url = URL(Constants.BASE_URL + "apps?action=list_home")
            val urlConnection = url.openConnection() as HttpsURLConnection
            urlConnection.requestMethod = Constants.REQUEST_METHOD
            urlConnection.connectTimeout = Constants.CONNECT_TIMEOUT
            urlConnection.readTimeout = Constants.READ_TIMEOUT
            val result = reader.readValue<HomeResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class HomeResult @JsonCreator
    constructor(@JsonProperty("success") private val success: Boolean,
                @JsonProperty("home") private val home: SubHomeResult) {

        fun getBannerApps(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(applicationManager, context, home.bannerApps)
        }

        fun getApps(applicationManager: ApplicationManager, context: Context): HashMap<Category, ArrayList<Application>> {
            val apps = HashMap<Category, ArrayList<Application>>()
            for (pair in home.apps) {
                apps[pair.key] = ApplicationParser.parseToApps(applicationManager, context, pair.value.toTypedArray())
            }
            return apps
        }

    }

    class SubHomeResult @JsonCreator
    constructor(@JsonProperty("banner_apps") val bannerApps: Array<FullData>) {

        val apps = HashMap<Category, ArrayList<FullData>>()

        @JsonAnySetter
        fun append(key: String, value: Any) {
            val apps = value as ArrayList<*>
            val appsData = ArrayList<FullData>()
            apps.forEach {
                val data = it as LinkedHashMap<*, *>
                val appData = FullData(
                        data["package_name"] as String,
                        data["_id"] as String,
                        data["name"] as String,
                        data["last_modified"] as String,
                        data["latest_version"] as String,
                        data["latest_version_number"] as String?,
                        data["latest_downloaded_version"].toString(),
                        data["author"] as String,
                        data["icon_image_path"] as String,
                        (data["other_images_path"] as List<String>).toTypedArray(),
                        data["category"] as String,
                        data["created_on"] as String,
                        data["source"] as String,
                        data["description"] as String,
                        data["app_link"] as String,
                        data["licence"] as String,
                        null)
                for (pair in data) {
                    appData.jsonCreator(pair.key as String, pair.value)
                }
                appsData.add(appData)
            }
            this.apps[Category(key)] = appsData
        }

    }

}