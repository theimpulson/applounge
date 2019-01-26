package foundation.e.apps.application.model

import foundation.e.apps.utils.Error

interface ApplicationStateListener {

    fun stateChanged(state: State)

    fun downloading(downloader: Downloader)

    fun anErrorHasOccurred(error: Error)

}
