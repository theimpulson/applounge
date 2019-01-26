package foundation.e.apps.application.model

import android.content.Context
import foundation.e.apps.application.model.data.BasicData
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Error
import java.util.*

class StateManager(private val info: ApplicationInfo, private val app: Application, private val appManager: ApplicationManager) {
    private var listeners = Collections.synchronizedList(ArrayList<ApplicationStateListener>())
    var state = State.NOT_DOWNLOADED
        private set

    fun find(context: Context, basicData: BasicData) {
        val state = if (appManager.isInstalling(app)) {
            State.INSTALLING
        } else if (info.isLastVersionInstalled(context,
                        basicData.lastVersionNumber ?: "")) {
            State.INSTALLED
        } else if (info.isInstalled(context) && !info.isLastVersionInstalled(context,
                        basicData.lastVersionNumber ?: "")) {
            State.NOT_UPDATED
        } else {
            State.NOT_DOWNLOADED
        }
        changeState(state)
    }

    private fun changeState(state: State) {
        this.state = state
        notifyStateChange()
    }

    fun addListener(listener: ApplicationStateListener) {
        listeners.add(listener)
        if (state == State.INSTALLING) {
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
