package io.eelo.appinstaller.application.model

import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import io.eelo.appinstaller.common.ProxyBitmap

class ApplicationData {

    //minimal data
    var packageName: String
    var id = ""
    val lastVersion: String
        get() = lastVersionObj.version
    val lastVersionObj: Version
        get() = versions[lastVersionName]!!

    //util data
    var lastModified = ""
    var name = ""
    var lastVersionName = ""
    var author = ""
    var icon = ""
    var iconImage: ProxyBitmap? = null
    var images = arrayOf<String>()

    //full data
    var description = ""
    var category = ""
    var createdOn = ""
    var source = ""
    var licence = ""
    var appLink = ""
    var versions = HashMap<String, Version>()
    var lastAccessed = ""

    //unsupported data
    var stars = 0f
    var privacyScore = 0

    var hasFullData = false

    constructor(packageName: String) {
        this.packageName = packageName
    }

    constructor(packageName: String,
                lastModified: String,
                id: String,
                name: String,
                lastVersionName: String,
                author: String,
                icon: String,
                images: Array<String>) {
        this.packageName = packageName
        this.lastModified = lastModified
        this.id = id
        this.name = name
        this.lastVersionName = lastVersionName
        this.author = author
        this.icon = icon
        this.images = images
        this.hasFullData = false
    }

    @Suppress("unused")
    @JsonCreator
    constructor(@JsonProperty("_id") id: String,
                @JsonProperty("icon_image_path") icon: String,
                @JsonProperty("package_name") packageName: String,
                @JsonProperty("created_on") createdOn: String,
                @JsonProperty("category") category: String,
                @JsonProperty("author") author: String,
                @JsonProperty("source") source: String,
                @JsonProperty("description") description: String,
                @JsonProperty("other_images_path") images: Array<String>,
                @JsonProperty("last_modified") lastModified: String,
                @JsonProperty("licence") licence: String,
                @JsonProperty("name") name: String,
                @JsonProperty("latest_version") lastVersionName: String,
                @JsonProperty("app_link") appLink: String,
                @JsonProperty("last_accessed") lastAccessed: String) {
        this.id = id
        this.icon = icon
        this.packageName = packageName
        this.createdOn = createdOn
        this.category = category
        this.author = author
        this.source = source
        this.description = description
        this.images = images
        this.lastModified = lastModified
        this.licence = licence
        this.name = name
        this.lastVersionName = lastVersionName
        this.appLink = appLink
        this.lastAccessed = lastAccessed
        hasFullData = true
    }

    @Suppress("unused")
    @JsonAnySetter
    fun updates(name: String, value: Any) {
        val result = value as LinkedHashMap<*, *>
        versions[name] = Version(result["downloaded_flag"] as String,
                result["eelo_download_link"] as String,
                result["min_android"] as String,
                result["apk_file_sha1"] as String,
                result["created_on"] as String,
                result["version"] as String,
                result["signature"] as String,
                result["apk_file_size"] as String,
                result["update_on"] as String,
                result["source_apk_download"] as String,
                result["whats_new"] as String?,
                name)
    }
}
