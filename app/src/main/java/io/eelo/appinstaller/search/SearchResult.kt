package io.eelo.appinstaller.search

import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.Application
import io.eelo.appinstaller.application.ApplicationManager
import java.util.*

class SearchResult(@param:JsonProperty("pages") val pages: Int,
                   @param:JsonProperty("numberOfResults") val results: Int,
                   @param:JsonProperty("apps") private val apps: Array<ApplicationResult>) {

    fun createApplicationsList(settings: Settings): List<ApplicationManager> {
        val managers = ArrayList<ApplicationManager>(apps.size)
        for (app in apps) {
            val data = app.createApplicationData()
            val manager = ApplicationManager(Application(settings, data))
            manager.findState()
            managers.add(manager)
        }
        return managers
    }
}
