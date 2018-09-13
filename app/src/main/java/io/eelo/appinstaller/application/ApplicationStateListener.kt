package io.eelo.appinstaller.application

interface ApplicationStateListener {

    fun stateChanged(state: State)

    fun downloading(downloader: Downloader)

    fun anErrorHasOccurred()

}
