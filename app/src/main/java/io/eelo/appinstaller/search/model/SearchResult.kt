package io.eelo.appinstaller.search.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import java.util.*

class SearchResult(@param:JsonProperty("pages") val pages: Int,
                   @param:JsonProperty("numberOfResults") val results: Int,
                   @param:JsonProperty("apps") private val appResuts: Array<ApplicationResult>) {

    fun createApplicationsList(installManager: InstallManager): List<Application> {
        val apps = ArrayList<Application>(appResuts.size)
        for (app in appResuts) {
            val data = app.createApplicationData()
            apps.add(installManager.findOrCreateApp(data))
        }
        return apps
    }
}
