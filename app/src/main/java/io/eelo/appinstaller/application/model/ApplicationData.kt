package io.eelo.appinstaller.application.model

import com.fasterxml.jackson.annotation.JsonAnySetter
import io.eelo.appinstaller.common.ProxyBitmap

class ApplicationData {

    //minimal data
    lateinit var packageName: String
    var id = ""
    val lastVersion: String
        get() = lastVersionObj.version
    val lastVersionObj: Version
        get() {
            return versions[lastVersionName]!!
        }

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
    var appType = ""
    var differenceInDownloads = 0
    var numberOfDownloads = 0

    //unsupported data
    var stars = 0f
    var privacyScore = 0
    var energyScore = 0

    var dataIndex = 0

    constructor(packageName: String) {
        this.packageName = packageName
        dataIndex = 0
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
        dataIndex = 1
    }

    constructor() {
        dataIndex = 2
    }

    @Suppress("unused")
    @JsonAnySetter
    fun jsonCreator(name: String, value: Any) {
        when (name) {
            "_id" -> id = value as String
            "icon_image_path" -> icon = value as String
            "package_name" -> packageName = value as String
            "created_on" -> createdOn = value as String
            "category" -> category = value as String
            "author" -> author = value as String
            "source" -> source = value as String
            "description" -> description = value as String
            "other_images_path" -> images = (value as List<String>).toTypedArray()
            "last_modified" -> lastModified = value as String
            "licence" -> licence = value as String
            "license" -> licence = value as String
            "name" -> this.name = value as String
            "latest_version" -> lastVersionName = value as String
            "app_link" -> appLink = value as String
            "last_accessed" -> lastAccessed = value as String
            "appType" -> appType = value as String
            "differenceInDownloads" -> differenceInDownloads = value as Int
            "number_of_downloads" -> numberOfDownloads = value as Int
            else -> {
                if (name.startsWith("update_")) {
                    val result = value as LinkedHashMap<*, *>
                    versions[name] = Version(result["downloaded_flag"] as String?,
                            result["eelo_download_link"] as String,
                            result["min_android"] as String?,
                            result["apk_file_sha1"] as String?,
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
        }
    }

    fun update(data: ApplicationData) {
        when (data.dataIndex) {
            0 -> {
                packageName = data.packageName
            }
            1 -> {
                if (dataIndex < 1) {
                    dataIndex = 1
                }
                packageName = data.packageName
                lastModified = data.lastModified
                id = data.id
                name = data.name
                lastVersionName = data.lastVersionName
                author = data.author
                icon = data.icon
                images = data.images
            }
            2 -> {
                dataIndex = 2
                id = data.id
                icon = data.icon
                packageName = data.packageName
                createdOn = data.createdOn
                category = data.category
                author = data.author
                source = data.source
                description = data.description
                images = data.images
                lastModified = data.lastModified
                licence = data.licence
                name = data.name
                lastVersionName = data.lastVersionName
                appLink = data.appLink
                lastAccessed = data.lastAccessed
                versions = data.versions
            }
        }
        if (data.iconImage != null) {
            iconImage = data.iconImage
        }
    }
}
