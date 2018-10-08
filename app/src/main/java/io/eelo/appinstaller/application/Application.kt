package io.eelo.appinstaller.application

import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.State.*
import java.io.IOException

import java.util.concurrent.atomic.AtomicInteger

class Application(private val settings: Settings, var data: ApplicationData) {

    private val uses = AtomicInteger(0)
    private val info = ApplicationInfo(settings, data)
    private val stateManager = StateManager(info)

    fun addListener(listener: ApplicationStateListener) {
        stateManager.addListener(listener)
    }

    fun removeListener(listener: ApplicationStateListener) {
        stateManager.removeListener(listener)
    }

    val state: State
        get() = stateManager.state

    fun incrementUses() {
        uses.incrementAndGet()
    }

    fun decrementUses() {
        uses.decrementAndGet();
        settings.installManager!!.tryRemove(this)
    }

    @Synchronized
    fun buttonClicked() {
        when (stateManager.state) {
            INSTALLED -> info.launch()
            DOWNLOADED -> {
                stateManager.changeState(INSTALLING)
                settings.installManager!!.install(data.packageName)
            }
            NOT_UPDATED, NOT_DOWNLOADED -> {
                stateManager.changeState(DOWNLOADING)
                settings.installManager!!.download(data.packageName)
            }
            DOWNLOADING -> {
            }
            INSTALLING -> {
            }
        }
    }

    fun download() {
        val downloader = info.createDownloader()
        stateManager.notifyDownloading(downloader)
        Thread {
            try {
                downloader.download()
                stateManager.changeState(INSTALLING)
                settings.installManager!!.install(data.packageName)
                stateManager.find()
            } catch (e: IOException) {
                stateManager.find()
                stateManager.notifyError()
            }
        }.start()
    }

    fun install() {
        info.install()
        stateManager.find()
    }

    fun isUsed(): Boolean {
        return uses.get() == 0
    }

}
