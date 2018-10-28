package io.eelo.appinstaller.search.model

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import java.util.*

class SearchResult @JsonCreator
constructor(@param:JsonProperty("pages") private val pages: Int,
            @param:JsonProperty("numberOfResults") private val results: Int,
            @param:JsonProperty("apps") private val appResults: Array<ApplicationResult>) {

    fun createApplicationsList(context: Context, installManager: InstallManager): List<Application> {
        val apps = ArrayList<Application>()
        for (applicationResult in appResults) {
            val data = applicationResult.createApplicationData()
            apps.add(installManager.findOrCreateApp(context, data))
        }
        return apps
    }
}
