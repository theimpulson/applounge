package io.eelo.appinstaller.search.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.model.ApplicationData

class ApplicationResult @JsonCreator
constructor(@param:JsonProperty("_id") private val packageName: String,
            @param:JsonProperty("id") private val id: String,
            @param:JsonProperty("name") private val name: String,
            @param:JsonProperty("textScore") private val score: Float,
            @param:JsonProperty("last_modified") private val lastModified: String,
            @param:JsonProperty("latest_version") private val lastVersion: String,
            @param:JsonProperty("author") private val author: String,
            @param:JsonProperty("icon_image_path") private val icon: String,
            @param:JsonProperty("other_images_path") private val images: Array<String>) {


    fun createApplicationData(): ApplicationData {
        return ApplicationData(packageName, lastModified, id, name, lastVersion, author, icon, images, score)
    }

}
