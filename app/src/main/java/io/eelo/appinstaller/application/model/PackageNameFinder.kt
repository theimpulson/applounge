package io.eelo.appinstaller.application.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class PackageNameFinder @JsonCreator
constructor(@JsonProperty("pages") val pages: Int,
            @JsonProperty("numberOfResults") val numberOfResults: Int,
            @JsonProperty("apps") val apps: Array<ApplicationData>) {

    companion object {
        private val reader = ObjectMapper().readerFor(PackageNameFinder::class.java)

        fun find(packageName: String): ApplicationData? {
            val url = URL(Constants.BASE_URL + "apps?action=search&by=package_name&keyword=$packageName")
            val result = reader.readValue<PackageNameFinder>(url.openStream())
            result.apps.forEach {
                println(url)
                if (it.packageName == packageName) {
                    return it
                }
            }
            return null
        }
    }

}