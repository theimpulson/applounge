package io.eelo.appinstaller.application.model

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.annotation.JsonAnySetter
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.utils.Constants
import io.eelo.appinstaller.utils.ImagesLoader
import java.net.URL

class ApplicationData {

    //minimal data
    lateinit var packageName: String

    //util data
    lateinit var lastModified: String
    private var id: String? = null
    lateinit var name: String
    lateinit var lastVersionName: String
    lateinit var author: String
    private lateinit var icon: String
    private var iconImage: Bitmap? = null
    lateinit var images: Array<String>
    private var imagesBitmaps: List<Bitmap>? = null

    //full data
    var description = ""
    var category = ""
    var createdOn = ""
    var source = ""
    var licence = ""
    var appLink = ""
    private var versions = HashMap<String, Version>()
    var lastAccessed = ""
    var appType = ""
    var differenceInDownloads = 0
    var numberOfDownloads = 0

    //unsupported data
    var stars = 0f
    var privacyScore = 0f
    var energyScore = 0f

    var fullnessLevel = 0

    constructor(packageName: String) {
        this.packageName = packageName
        fullnessLevel = 0
    }

    constructor(packageName: String,
                lastModified: String,
                id: String,
                name: String,
                lastVersionName: String,
                author: String,
                icon: String,
                images: Array<String>,
                score: Float) {
        this.packageName = packageName
        this.lastModified = lastModified
        this.id = id
        this.name = name
        this.lastVersionName = lastVersionName
        this.author = author
        this.icon = icon
        this.images = images
        this.stars = score
        fullnessLevel = 1
    }

    constructor() {
        fullnessLevel = 2
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
        when (data.fullnessLevel) {
            0 -> {
                packageName = data.packageName
            }
            1 -> {
                if (fullnessLevel < 1) {
                    fullnessLevel = 1
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
                fullnessLevel = 2
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

    fun loadIcon(): Bitmap {
        assertFullData()
        if (iconImage == null) {
            val url = URL(Constants.BASE_URL + "media/" + icon)
            iconImage = BitmapFactory.decodeStream(url.openStream())
        }
        return iconImage!!
    }

    fun loadImages(): List<Bitmap> {
        assertFullData()
        if (imagesBitmaps == null) {
            imagesBitmaps = ImagesLoader(images.toList()).loadImages()
        }
        return imagesBitmaps!!
    }

    fun loadLatestVersion(): Version {
        assertFullData()
        return versions[lastVersionName]!!
    }

    companion object {
        private val reader = ObjectMapper().readerFor(ApplicationData::class.java)!!
    }

    fun assertFullData(): Boolean {
        when (fullnessLevel) {
            1 -> {
                val newData = reader.readValue<ApplicationData>(URL(Constants.BASE_URL + "apps?action=app_detail&id=" + id))
                update(newData)
            }
            0 -> {
                val data = PackageNameFinder.find(packageName)
                if (data != null) {
                    update(data)
                }
                return false
            }
        }
        return true
    }
}
