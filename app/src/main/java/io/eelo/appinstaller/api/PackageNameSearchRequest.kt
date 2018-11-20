package io.eelo.appinstaller.api

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class PackageNameSearchRequest(private val packageName: String) {

    companion object {
        private val reader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun request(): SearchResult {
        return reader.readValue(URL(Constants.BASE_URL + "apps?action=search&keyword=$packageName&by=package_name"))
    }

    class SearchResult @JsonCreator
    constructor(@param:JsonProperty("pages") val pages: Int,
                @param:JsonProperty("numberOfResults") val resultsNumber: Int,
                @param:JsonProperty("apps") val appResults: Array<FullData>) {

        fun findOneAppData(packageName: String): FullData? {
            appResults.forEach {
                if (it.packageName == packageName) {
                    return it
                }
            }
            return null
        }
    }
}