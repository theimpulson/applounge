package io.eelo.appinstaller.application.model

import java.util.*

class StateManager(private val app: ApplicationInfo) {
    private var listeners = Collections.synchronizedList(ArrayList<ApplicationStateListener>())
    var state = State.NOT_DOWNLOADED
        private set

    init {
        find()
    }

    fun find() {
        changeState(if (app.isLastVersionInstalled) {
            State.INSTALLED
        } else if (app.isInstalled) {
            if (app.isDownloaded) State.DOWNLOADED else State.NOT_UPDATED
        } else if (app.isDownloaded) {
            State.DOWNLOADED
        } else {
            State.NOT_DOWNLOADED
        })
    }

    fun changeState(state: State) {
        this.state = state
        notifyStateChange()
    }

    fun addListener(listener: ApplicationStateListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: ApplicationStateListener) {
        listeners.remove(listener)
    }

    fun notifyDownloading(downloader: Downloader) {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.downloading(downloader)
        }
    }

    fun notifyError() {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.anErrorHasOccurred()
        }
    }

    private fun notifyStateChange() {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.stateChanged(state)
        }
    }
}
