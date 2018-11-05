package io.eelo.appinstaller.home.model

import android.content.Context
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.ApplicationParser.Companion.parseToApps

class HomeResult @JsonCreator
constructor(@JsonProperty("success") val success: Boolean,
            @JsonProperty("home") private val child: SubHomeResult) {

    fun bannerApps(installManager: InstallManager, context: Context): ArrayList<Application> {
        return parseToApps(installManager, context, child.bannerApps)
    }

    fun parseApplications(installManager: InstallManager, context: Context): HashMap<String, List<Application>> {
        val result = HashMap<String, List<Application>>()
        child.apps.forEach {
            result[it.key] = parseToApps(installManager, context, it.value.toTypedArray())
        }
        return result
    }

    class SubHomeResult @JsonCreator
    constructor(@JsonProperty("banner_apps") val bannerApps: Array<ApplicationData>) {

        val apps = HashMap<String, List<ApplicationData>>()

        @JsonAnySetter
        fun applications(key: String, value: Any) {
            val apps = value as ArrayList<*>
            val appsData = ArrayList<ApplicationData>()
            apps.forEach {
                val data = it as LinkedHashMap<*, *>
                val appData = ApplicationData()
                data.forEach { name, value ->
                    appData.jsonCreator(name as String, value)
                }
                appsData.add(appData)
            }
            this.apps[key] = appsData
        }

    }


}