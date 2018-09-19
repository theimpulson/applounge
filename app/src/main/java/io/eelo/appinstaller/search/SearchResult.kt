package io.eelo.appinstaller.search

import android.content.Context
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.Application
import io.eelo.appinstaller.application.ApplicationManager
import java.util.*

class SearchResult(@param:JsonProperty("pages") val pages: Int,
                   @param:JsonProperty("numberOfResults") val results: Int,
                   @param:JsonProperty("apps") private val apps: Array<ApplicationResult>) {

    fun createApplicationsList(APKsFolder: String, serverURL: String, context: Context): List<ApplicationManager> {
        val managers = ArrayList<ApplicationManager>(apps.size)
        for (app in apps) {
            val data = app.createApplicationData()
            managers.add(ApplicationManager(Application(APKsFolder, serverURL, data, context)))
        }
        return managers
    }
}
