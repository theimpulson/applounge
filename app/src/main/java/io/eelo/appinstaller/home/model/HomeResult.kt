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

    fun parseApplications(installManager: InstallManager, context: Context): LinkedHashMap<String, ArrayList<Application>> {
        val result = LinkedHashMap<String, ArrayList<Application>>()
        child.apps.forEach {
            result[it.key] = parseToApps(installManager, context, it.value.toTypedArray())
        }
        return result
    }

    class SubHomeResult @JsonCreator
    constructor(@JsonProperty("banner_apps") val bannerApps: Array<ApplicationData>) {

        val apps = LinkedHashMap<String, ArrayList<ApplicationData>>()

        @JsonAnySetter
        fun applications(key: String, value: Any) {
            val apps = value as ArrayList<*>
            val appsData = ArrayList<ApplicationData>()
            apps.forEach {
                val data = it as LinkedHashMap<*, *>
                val appData = ApplicationData()
                for (pair in data) {
                    appData.jsonCreator(pair.key as String, pair.value)
                }
                appsData.add(appData)
            }
            this.apps[key] = appsData
        }

    }
}
