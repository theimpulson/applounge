package io.eelo.appinstaller.application.model

enum class State(val buttonText: String) {
    NOT_DOWNLOADED("install"),
    NOT_UPDATED("update"),
    DOWNLOADING("downloading"),
    DOWNLOADED("install"),
    INSTALLING("installing"),
    INSTALLED("open");
}
