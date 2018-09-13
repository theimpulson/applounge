package io.eelo.appinstaller.search

import com.fasterxml.jackson.annotation.JsonProperty

import io.eelo.appinstaller.application.ApplicationData

class ApplicationResult(@param:JsonProperty("_id") private val packageName: String,
                        @param:JsonProperty("id") private val id: String,
                        @param:JsonProperty("name") private val name: String,
                        @param:JsonProperty("textScore") private val stars: Float,
                        @param:JsonProperty("last_modified") private val lastModified: String,
                        @param:JsonProperty("lastest_version") private val lastVersion: String,
                        @param:JsonProperty("author") private val author: String,
                        @param:JsonProperty("icon_image_path") private val icon: String,
                        @param:JsonProperty("other_images_path") private val images: Array<String>) {

    fun createApplicationData(): ApplicationData {
        val applicationData = ApplicationData(packageName, lastVersion)
        applicationData.name = name
        applicationData.author = author
        applicationData.icon = icon
        applicationData.images = images
        applicationData.id = id
        applicationData.stars = stars
        applicationData.lastModified = lastModified
        return applicationData
    }

}
