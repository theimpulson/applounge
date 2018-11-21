package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class SearchRequest(private val keyword: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun request(): SearchResult {
        return reader.readValue(URL(Constants.BASE_URL + "apps?action=search&keyword=$keyword&page=$page&nres=$resultsPerPage"))
    }

    class SearchResult @JsonCreator
    constructor(@param:JsonProperty("pages") val pages: Int,
                @param:JsonProperty("numberOfResults") val resultsNumber: Int,
                @param:JsonProperty("apps") val appResults: Array<BasicData>) {

        fun getApplications(installManager: InstallManager, context: Context): List<Application> {
            return ApplicationParser.parseToApps(installManager, context, appResults)
        }
    }
}