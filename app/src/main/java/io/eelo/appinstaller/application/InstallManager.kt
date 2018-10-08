package io.eelo.appinstaller.application

import io.eelo.appinstaller.Settings
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class InstallManager(private val settings: Settings) {

    private val apps = HashMap<String, Application>()
    private val downloading = ArrayBlockingQueue<String>(Int.MAX_VALUE)
    private val installing = ArrayBlockingQueue<String>(Int.MAX_VALUE)

    @Synchronized
    fun findOrCreateApp(data: ApplicationData): Application {
        if (apps.containsKey(data.packageName)) {
            apps[data.packageName] = Application(settings, data)
        }
        val app = apps[data.packageName]!!
        app.incrementUses()
        return app
    }

    @Synchronized
    fun download(packageName: String) {
        if (!downloading.contains(packageName)) {
            downloading.put(packageName)
        }
    }

    @Synchronized
    fun install(packageName: String) {
        if (!installing.contains(packageName)) {
            installing.put(packageName)
        }
    }

    fun start() {
        Thread {
            startDownloads()
        }.start()
        Thread {
            startInstalls()
        }.start()
    }

    private fun startDownloads() {
        while (true) {
            val app = apps[downloading.take()]!!
            app.download()
            tryRemove(app)
        }
    }

    private fun startInstalls() {
        while (true) {
            val app = apps[installing.take()]!!
            app.install()
            tryRemove(app)
        }
    }

    fun tryRemove(app: Application) {
        val packageName = app.data.packageName
        if (!app.isUsed() && !installing.contains(packageName) && !downloading.contains(packageName)) {
            apps.remove(packageName)
        }
    }


}