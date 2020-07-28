package foundation.e.apps.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnore
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
        private val reader = Common.getObjectMapper().readerFor(HomeResult::class.java)
    }

    fun request(callback: (Error?, HomeResult?) -> Unit) {
        try {
            var appType = MainActivity.mActivity.showApplicationTypePreference()
            val url = Constants.BASE_URL + "apps?action=list_home&type=$appType"
            val urlConnection = Common.createConnection(url, Constants.REQUEST_METHOD_GET)
            val result = reader.readValue<HomeResult>(urlConnection.inputStream)
            urlConnection.disconnect()
            callback.invoke(null, result)
        } catch (e: Exception) {
            callback.invoke(Error.findError(e), null)
        }
    }

    class HomeResult @JsonCreator
    constructor(@JsonProperty("home") private val home: PwasSubHomeResult) {
        fun getBannerApps(applicationManager: ApplicationManager, context: Context): ArrayList<Application> {
            return ApplicationParser.PwaParseToApps(applicationManager, context, home.bannerApps)
        }

        fun getApps(applicationManager: ApplicationManager, context: Context): LinkedHashMap<Category, ArrayList<Application>> {
            val apps = LinkedHashMap<Category, ArrayList<Application>>()
            for (pair in home.apps) {
                if(pair.value .isEmpty() ){
                    apps.remove(pair.key)
                }else {

                    apps[pair.key] = ApplicationParser.PwaParseToApps(applicationManager, context, pair.value.toTypedArray())
                }
            }
            return apps
        }
    }

    class PwasSubHomeResult @JsonCreator constructor() {
        @JsonIgnore
        val apps = LinkedHashMap<Category, ArrayList<PwasBasicData>>()
        lateinit var bannerApps: Array<PwasBasicData>

        @JsonAnySetter
        fun append(key: String, value: Any) {
            val apps = value as ArrayList<*>
            val appsData = ArrayList<PwasBasicData>()
            apps.forEach {
                val data = it as LinkedHashMap<*, *>
                val appData = PwasBasicData(
                        data["_id"] as String,
                        data["name"] as String,
                        data["description"] as String?,
                        data["is_pwa"] as Boolean,
                        data["is_web_app"] as Boolean,
                        data["has_https"] as Boolean,
                        data["url"] as String?,
                        data["category"] as String,
                        data["icon_image_path"] as String,
                        (data["other_images_path"]as List<String>).toTypedArray(),
                        data["created_on"] as String?)
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