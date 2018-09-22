package io.eelo.appinstaller.application

interface ApplicationStateListener {

    class EMPTY : ApplicationStateListener {
        override fun downloading(downloader: Downloader) {}
        override fun anErrorHasOccurred() {}
        override fun stateChanged(state: State) {}
    }

    fun stateChanged(state: State)

    fun downloading(downloader: Downloader)

    fun anErrorHasOccurred()

}
