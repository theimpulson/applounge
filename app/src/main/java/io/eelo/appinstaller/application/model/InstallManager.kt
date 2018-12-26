package io.eelo.appinstaller.application.model

import android.content.Context
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class InstallManager {

    private val apps = HashMap<String, Application>()
    private val downloading = ArrayBlockingQueue<String>(100)
    private val installing = ArrayBlockingQueue<String>(100)

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
            val app = apps[downloading.take()]!!
            app.download(context)
            tryRemove(app)
        }
    }

    private fun startInstalls(context: Context) {
        while (true) {
            val app = apps[installing.take()]!!
            app.install(context)
            tryRemove(app)
        }
    }

    fun tryRemove(app: Application) {
        if (!app.isUsed() && !installing.contains(app.packageName) && !downloading.contains(app.packageName)) {
            apps.remove(app.packageName)
        }
    }
}
