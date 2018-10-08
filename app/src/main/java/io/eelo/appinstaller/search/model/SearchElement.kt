package io.eelo.appinstaller.search.model

import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.Application
import io.eelo.appinstaller.utlis.Constants
import java.io.IOException
import java.net.URL
import java.util.*

class SearchElement(private val query: String, private val settings: Settings) {

    val apps = ArrayList<Application>()
    private var nextPage = 0

    companion object {
        private val jsonReader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    @Throws(IOException::class)
    fun search() {
        val url = URL(Constants.BASE_URL + "apps?action=search&keyword=" + query + "&page=" + nextPage + "&nres=" + Constants.RESULTS_PER_PAGE)
        val result = jsonReader.readValue<SearchResult>(url.openStream())
        val addingApps = result.createApplicationsList(settings)
        apps.addAll(addingApps)
        nextPage++
    }

}