package io.eelo.appinstaller.application.model.data

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.application.model.Version

class FullData @JsonCreator
constructor(
        @JsonProperty("package_name") packageName: String,
        @JsonProperty("_id") id: String,
        @JsonProperty("name") name: String,
        @JsonProperty("last_modified") lastModified: String,
        @JsonProperty("latest_version") lastVersion: String,
        @JsonProperty("author") author: String,
        @JsonProperty("icon_image_path") iconUri: String,
        @JsonProperty("other_images_path") imagesUri: Array<String>,
        @JsonProperty("category") val category: String,
        @JsonProperty("created_on") val createdOn: String,
        @JsonProperty("source") val source: String,
        @JsonProperty("description") val description: String,
        @JsonProperty("app_link") val appLink: String,
        @JsonProperty("licence") val licence: String) {
//        @JsonProperty("textScore") score: Float,
//        @JsonProperty("privScore") val privacyScore: Float,
//        @JsonProperty("enerScore") val energyScore: Float) {

    var basicData = BasicData(packageName, id, name, -1f, lastModified, lastVersion, "", author, iconUri, imagesUri)
    private val versions = HashMap<String, Version>()
    val packageName: String
        get() = basicData.packageName
    val privacyScore = -1f
    val energyScore = -1f


    fun getLastVersion(): Version {
        return versions[basicData.lastVersion]!!
    }

    @Suppress("unused")
    @JsonAnySetter
    fun jsonCreator(name: String, value: Any) {
        if (name.startsWith("update_")) {
            val result = value as LinkedHashMap<*, *>
            versions[name] = Version(result["downloaded_flag"] as String?,
                    result["eelo_download_link"] as String,
                    result["min_android"] as String,
                    result["apk_file_sha1"] as String?,
                    result["created_on"] as String,
                    result["version"] as String,
                    result["signature"] as String,
                    result["apk_file_size"] as String,
                    result["update_on"] as String,
                    result["source_apk_download"] as String,
                    result["whats_new"] as String?,
                    name)
            if (name == basicData.lastVersion) {
                basicData.lastVersionNumber = result["version"] as String
            }
        }
    }
}