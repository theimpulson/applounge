package io.eelo.appinstaller.search

import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.ApplicationManager
import java.io.IOException
import java.net.URL
import java.util.*

class SearchElement(private val query: String, private val settings: Settings) {

    val apps = ArrayList<ApplicationManager>()
    private var nextPage = 0

    companion object {
        private val jsonReader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    @Throws(IOException::class)
    fun search() {
        val url = URL(settings.serverPath + "apps?action=search&keyword=" + query + "&page=" + nextPage + "&nres=" + settings.resultsPerPage)
        val result = jsonReader.readValue<SearchResult>(url.openStream())
        val addingApps = result.createApplicationsList(settings)
        apps.addAll(addingApps)
        nextPage++
    }

}