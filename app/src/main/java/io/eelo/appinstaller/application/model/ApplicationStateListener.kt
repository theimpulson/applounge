package io.eelo.appinstaller.application.model

import io.eelo.appinstaller.utils.Error

interface ApplicationStateListener {

    fun stateChanged(state: State)

    fun downloading(downloader: Downloader)

    fun anErrorHasOccurred(error: Error)

}
