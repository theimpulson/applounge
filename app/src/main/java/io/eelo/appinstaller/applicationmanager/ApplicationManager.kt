package io.eelo.appinstaller.applicationmanager

import android.content.Context
import io.eelo.appinstaller.application.model.Application
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class ApplicationManager {

    private val apps = HashMap<String, Application>()
    private val downloading = LinkedBlockingQueue<Application>()
    private val installing = LinkedBlockingQueue<Application>()

    @Synchronized
    fun findOrCreateApp(packageName: String): Application {
        if (!apps.containsKey(packageName)) {
            apps[packageName] = Application(packageName, this)
        }
        val app = apps[packageName]!!
        app.incrementUses()
        return app
    }

    @Synchronized
    fun download(app: Application) {
        if (!downloading.contains(app)) {
            downloading.put(app)
            downloading.put(app)
        }
    }

    @Synchronized
    fun install(app: Application) {
        if (!installing.contains(app)) {
            installing.put(app)
            installing.put(app)
        }
    }

    fun start(context: Context) {
        Thread {
            startDownloads(context)
        }.start()
        Thread {
            startInstalls(context)
        }.start()
    }

    private fun startDownloads(context: Context) {
        while (true) {
            val app = downloading.take()
            app.download(context)
            stopDownloading(app)
            tryRemove(app)
        }
    }

    private fun startInstalls(context: Context) {
        while (true) {
            val app = installing.take()
            app.install(context)
            stopInstalling(app)
            tryRemove(app)
        }
    }

    fun tryRemove(app: Application) {
        if (!app.isUsed() && !installing.contains(app) && !downloading.contains(app)) {
            apps.remove(app.packageName)
        }
    }

    fun stopDownloading(app: Application) {
        while (downloading.remove(app)) {
        }
    }

    fun stopInstalling(app: Application) {
        while (installing.remove(app)) {
        }
    }

    fun isInstalling(app: Application): Boolean {
        return installing.contains(app)
    }

    fun isDownloading(app: Application): Boolean {
        return downloading.contains(app)
    }
}
