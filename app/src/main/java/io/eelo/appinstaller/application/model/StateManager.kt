package io.eelo.appinstaller.application.model

import android.content.Context
import io.eelo.appinstaller.application.model.data.BasicData
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Error
import java.util.*

class StateManager(private val info: ApplicationInfo, private val app: Application, private val appManager: ApplicationManager) {
    private var listeners = Collections.synchronizedList(ArrayList<ApplicationStateListener>())
    var state = State.NOT_DOWNLOADED
        private set

    fun find(context: Context, basicData: BasicData) {
        val state: State
        if (appManager.isDownloading(app)) {
            state = State.DOWNLOADING
        } else if (appManager.isInstalling(app)) {
            state = State.INSTALLING
        } else if (info.isLastVersionInstalled(context,
                        basicData.lastVersionNumber ?: "")) {
            state = State.INSTALLED
            info.getApkFile(context, basicData).delete()
        } else if (info.isInstalled(context) && !info.isLastVersionInstalled(context,
                        basicData.lastVersionNumber ?: "")) {
            state = State.NOT_UPDATED
            info.getApkFile(context, basicData).delete()
        } else {
            state = State.NOT_DOWNLOADED
            info.getApkFile(context, basicData).delete()
        }
        changeState(state)
    }

    private fun changeState(state: State) {
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

    fun notifyError(error: Error) {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.anErrorHasOccurred(error)
        }
    }

    private fun notifyStateChange() {
        listeners.forEach { listener: ApplicationStateListener ->
            listener.stateChanged(state)
        }
    }
}
