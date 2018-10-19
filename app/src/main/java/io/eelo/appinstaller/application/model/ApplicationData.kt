package io.eelo.appinstaller.application.model

class ApplicationData {

    var packageName: String
    var stars = 0f
    var lastModified = ""
    var id = ""
    var name = ""
    var lastVersion: String
    var author = ""
    var icon = ""
    var images = arrayOf<String>()

    var description = ""
    var category = ""
    var createdOn = ""
    var numberOfDownloads = 0
    var source = ""
    var license = ""
    var privacyScore = 0

    var hasFullData = false

    constructor(packageName: String,
                stars: Float,
                lastModified: String,
                id: String,
                name: String,
                lastVersion: String,
                author: String,
                icon: String,
                images: Array<String>) {
        this.packageName = packageName
        this.stars = stars
        this.lastModified = lastModified
        this.id = id
        this.name = name
        this.lastVersion = lastVersion
        this.author = author
        this.icon = icon
        this.images = images
        this.hasFullData = false
    }
}
