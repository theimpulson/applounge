package io.eelo.appinstaller.application

class StateManager(private val app: Application) {
    var listener: ApplicationStateListener = ApplicationStateListener.EMPTY()
    var state = State.NOT_DOWNLOADED
        private set

    fun find() {
        state = if (app.isLastVersionInstalled) {
            State.INSTALLED
        } else if (app.isInstalled) {
            if (app.isDownloaded) State.DOWNLOADED else State.NOT_UPDATED
        } else if (app.isDownloaded) {
            State.DOWNLOADED
        } else {
            State.NOT_DOWNLOADED
        }
        listener.stateChanged(state)
    }

    fun changeState(state: State) {
        this.state = state
        listener.stateChanged(state)
    }
}
