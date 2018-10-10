package io.eelo.appinstaller.application

import android.content.Context
import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.State.*
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class Application(var data: ApplicationData, context: Context, settings: Settings, private val installManager: InstallManager) {

    private val uses = AtomicInteger(0)
    private val info = ApplicationInfo(data, settings, context)
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
        installManager.tryRemove(this)
    }

    @Synchronized
    fun buttonClicked() {
        when (stateManager.state) {
            INSTALLED -> info.launch()
            DOWNLOADED -> {
                stateManager.changeState(INSTALLING)
                installManager.install(data.packageName)
            }
            NOT_UPDATED, NOT_DOWNLOADED -> {
                stateManager.changeState(DOWNLOADING)
                installManager.download(data.packageName)
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
                installManager.install(data.packageName)
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
