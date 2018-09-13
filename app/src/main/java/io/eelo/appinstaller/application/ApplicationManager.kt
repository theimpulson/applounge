package io.eelo.appinstaller.application

import java.io.IOException

import io.eelo.appinstaller.application.State.DOWNLOADING
import io.eelo.appinstaller.application.State.INSTALLING

class ApplicationManager(private val app: Application) {
    private var stateManager: StateManager? = null
    private var listener: ApplicationStateListener? = null

    val data: ApplicationData
        get() = app.data

    fun initialize(listener: ApplicationStateListener) {
        this.listener = listener
        stateManager = StateManager(app, listener)
        stateManager!!.find()
    }

    @Synchronized
    fun buttonClicked() {
        when (stateManager!!.state) {
            State.INSTALLED -> app.launch()
            State.DOWNLOADED -> {
                stateManager!!.changeState(INSTALLING)
                app.install()
                stateManager!!.find()
            }
            State.NOT_UPDATED, State.NOT_DOWNLOADED -> {
                stateManager!!.changeState(DOWNLOADING)
                download()
            }
        }
    }

    private fun download() {
        val downloader = app.createDownloader()
        listener!!.downloading(downloader)
        Thread {
            try {
                downloader.download()
                stateManager!!.changeState(INSTALLING)
                app.install()
                stateManager!!.find()
            } catch (e: IOException) {
                stateManager!!.find()
                listener!!.anErrorHasOccurred()
            }
        }.start()
    }

}
