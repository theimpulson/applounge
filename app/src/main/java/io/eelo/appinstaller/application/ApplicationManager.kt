package io.eelo.appinstaller.application

import io.eelo.appinstaller.application.State.DOWNLOADING
import io.eelo.appinstaller.application.State.INSTALLING
import java.io.IOException

class ApplicationManager(private val app: Application) {
    private val stateManager = StateManager(app)

    val data: ApplicationData
        get() = app.data

    fun findState() {
        stateManager.find()
    }

    fun setListener(listener: ApplicationStateListener) {
        stateManager.listener = listener
    }

    val state: State
        get() = stateManager.state

    @Synchronized
    fun buttonClicked() {
        when (stateManager.state) {
            State.INSTALLED -> app.launch()
            State.DOWNLOADED -> {
                stateManager.changeState(INSTALLING)
                app.install()
                stateManager.find()
            }
            State.NOT_UPDATED, State.NOT_DOWNLOADED -> {
                stateManager.changeState(DOWNLOADING)
                download()
            }
        }
    }

    private fun download() {
        val downloader = app.createDownloader()
        stateManager.listener.downloading(downloader)
        Thread {
            try {
                downloader.download()
                stateManager.changeState(INSTALLING)
                app.install()
                stateManager.find()
            } catch (e: IOException) {
                stateManager.find()
                stateManager.listener.anErrorHasOccurred()
            }
        }.start()
    }

}
