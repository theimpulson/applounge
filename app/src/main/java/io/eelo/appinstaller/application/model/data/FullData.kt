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
        @JsonProperty("licence") val licence: String,
        @JsonProperty("ratings") ratings: BasicData.Ratings?) {

    var basicData = if (ratings == null) {
        BasicData(packageName, id, name, -1f, lastModified, lastVersion,
                "", author, iconUri, imagesUri,
                BasicData.Ratings(-1f, -1f))
    } else {
        BasicData(packageName, id, name, -1f, lastModified, lastVersion,
                "", author, iconUri, imagesUri,
                ratings)
    }
    private val versions = HashMap<String, Version>()
    val packageName: String
        get() = basicData.packageName

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
                    name,
                    result["exodus_score"] as Int?,
                    getPermissions(result["exodus_perms"] as ArrayList<String>?),
                    getTrackers(result["exodus_trackers"] as ArrayList<LinkedHashMap<String, String>>?))
            if (name == basicData.lastVersion) {
                basicData.lastVersionNumber = result["version"] as String
            }
        }
    }

    private fun getPermissions(rawPermissions: ArrayList<String>?): ArrayList<String> {
        val permissions = ArrayList<String>()
        rawPermissions?.forEach {
            permissions.add(it.substring(it.lastIndexOf(".") + 1))
        }
        return permissions
    }

    private fun getTrackers(rawTrackers: ArrayList<LinkedHashMap<String, String>>?): ArrayList<String> {
        val trackers = ArrayList<String>()
        rawTrackers?.forEach {
            trackers.add(it["name"]!!)
        }
        return trackers
    }
}