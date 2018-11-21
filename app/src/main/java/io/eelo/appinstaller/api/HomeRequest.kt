package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class HomeRequest {

    companion object {
        private val reader = ObjectMapper().readerFor(HomeResult::class.java)
    }

    fun request(): HomeResult {
        return reader.readValue(URL(Constants.BASE_URL + "apps?action=list_home"))
    }

    class HomeResult @JsonCreator
    constructor(@JsonProperty("success") private val success: Boolean,
                @JsonProperty("home") private val home: SubHomeResult) {

        fun getBannerApps(installManager: InstallManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(installManager, context, home.bannerApps)
        }

        fun getApps(installManager: InstallManager, context: Context): HashMap<String, ArrayList<Application>> {
            val apps = HashMap<String, ArrayList<Application>>()
            home.apps.forEach { category, appsData ->
                apps[category] = ApplicationParser.parseToApps(installManager, context, appsData.toTypedArray())
            }
            return apps
        }

    }

    class SubHomeResult @JsonCreator
    constructor(@JsonProperty("banner_apps") val bannerApps: Array<FullData>) {

        val apps = HashMap<String, ArrayList<FullData>>()

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
                        data["author"] as String,
                        data["icon_image_path"] as String,
                        (data["other_images_path"] as List<String>).toTypedArray(),
                        data["category"] as String,
                        data["created_on"] as String,
                        data["source"] as String,
                        data["description"] as String,
                        data["app_link"] as String,
                        data["licence"] as String)
                for (pair in data) {
                    appData.jsonCreator(pair.key as String, pair.value)
                }
                appsData.add(appData)
            }
            this.apps[key] = appsData
        }

    }

}