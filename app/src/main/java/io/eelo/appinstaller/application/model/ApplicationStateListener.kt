package io.eelo.appinstaller.application.model

interface ApplicationStateListener {

    fun stateChanged(state: State)

    fun downloading(downloader: Downloader)

    fun anErrorHasOccurred()

}