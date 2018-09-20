package io.eelo.appinstaller.application

class ApplicationData(var packageName: String, var lastVersion: String) {

    var name = ""
    var description = ""
    var category = ""
    var author = ""
    var createdOn = ""
    var numberOfDownloads = 0
    var source = ""
    var license = ""
    var stars = 0f
    var icon = ""
    var images = arrayOf<String>()
    var id = ""
    var lastModified = ""
    var privacyScore = 0
}
