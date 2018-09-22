package io.eelo.appinstaller.application

enum class State(val buttonText: String) {
    NOT_DOWNLOADED("install"),
    NOT_UPDATED("install"),
    DOWNLOADING("install"),
    DOWNLOADED("install"),
    INSTALLING("install"),
    INSTALLED("install");
}
