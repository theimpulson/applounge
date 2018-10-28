package io.eelo.appinstaller.application.model

import android.content.Context
import java.util.*

class StateManager(context: Context, private val info: ApplicationInfo, private val app: Application) {
    private var listeners = Collections.synchronizedList(ArrayList<ApplicationStateListener>())
    var state = State.NOT_DOWNLOADED
        private set

    init {
        find(context)
    }

    fun find(context: Context) {
        changeState(if (info.isLastVersionInstalled(context)) {
            State.INSTALLED
        } else if (info.isInstalled(context)) {
            if (info.isDownloaded) State.DOWNLOADED else State.NOT_UPDATED
        } else if (info.isDownloaded) {
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
        if (state == State.DOWNLOADING) {
            app.downloader?.let { listener.downloading(it) }
        }
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
