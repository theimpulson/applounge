package io.eelo.appinstaller.api

import android.content.Context
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.data.FullData
import io.eelo.appinstaller.utils.ApplicationParser
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class ListApplicationsRequest(private val category: String, private val page: Int, private val resultsPerPage: Int) {

    companion object {
        private val reader = ObjectMapper().readerFor(ListApplicationsResult::class.java)
    }

    fun request(): ListApplicationsResult {
        return reader.readValue(URL(Constants.BASE_URL + "apps?action=list_apps&category=$category&nres=$resultsPerPage&page=$page"))
    }

    class ListApplicationsResult @JsonCreator
    constructor(@JsonProperty("pages") val pages: Int,
                @JsonProperty("apps") private val apps: Array<FullData>) {

        fun getApplications(installManager: InstallManager, context: Context): ArrayList<Application> {
            return ApplicationParser.parseToApps(installManager, context, apps)
        }
    }


}