package io.eelo.appinstaller.application

enum class State(val buttonText: String) {
    NOT_DOWNLOADED("download"),
    NOT_UPDATED("update"),
    DOWNLOADING("downloading"),
    DOWNLOADED("install"),
    INSTALLING("installing"),
    INSTALLED("launch");
}
