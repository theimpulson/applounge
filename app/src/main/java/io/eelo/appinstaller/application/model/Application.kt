package io.eelo.appinstaller.application.model

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.State.*
import io.eelo.appinstaller.utils.Constants
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

class Application(val data: ApplicationData, context: Context, private val installManager: InstallManager) {

    private val uses = AtomicInteger(0)
    private val info = ApplicationInfo(data)
    private val stateManager = StateManager(info, this)

    init {
        if (data.id != "" && data.fullnessLevel != 2) {
            searchFullData()
            stateManager.find(context)
        }
    }

    fun addListener(listener: ApplicationStateListener) {
        stateManager.addListener(listener)
    }

    fun removeListener(listener: ApplicationStateListener) {
        stateManager.removeListener(listener)
    }

    val state: State
        get() = stateManager.state
    var downloader: Downloader? = null

    fun incrementUses() {
        uses.incrementAndGet()
    }

    fun decrementUses() {
        uses.decrementAndGet()
        installManager.tryRemove(this)
    }

    @Synchronized
    fun buttonClicked(context: Context) {
        when (stateManager.state) {
            INSTALLED -> info.launch(context)
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

    fun download(context: Context) {
        searchFullData()
        downloader = info.createDownloader()
        stateManager.notifyDownloading(downloader!!)
        try {
            downloader!!.download()
            stateManager.changeState(INSTALLING)
            installManager.install(data.packageName)
            stateManager.find(context)
        } catch (e: IOException) {
            e.printStackTrace()
            stateManager.find(context)
            stateManager.notifyError()
        }
    }

    fun install(context: Context) {
        info.install(context)
        stateManager.find(context)
    }

    fun isUsed(): Boolean {
        return uses.get() != 0
    }

    companion object {
        private val dataReader = ObjectMapper().readerFor(ApplicationData::class.java)
    }

    fun searchFullData() {
        val newData = dataReader.readValue<ApplicationData>(URL(Constants.BASE_URL + "apps?action=app_detail&id=" + data.id))
        data.update(newData)
    }
}
