package io.eelo.appinstaller.application.model

import android.content.Context
import java.util.*
import java.util.concurrent.ArrayBlockingQueue

class InstallManager {

    private val apps = HashMap<String, Application>()
    private val downloading = ArrayBlockingQueue<String>(1000)
    private val installing = ArrayBlockingQueue<String>(1000)

    @Synchronized
    fun findOrCreateApp(context: Context, data: ApplicationData): Application {
        if (!apps.containsKey(data.packageName)) {
            apps[data.packageName] = Application(data, context, this)
        } else {
            apps[data.packageName]!!.data.update(data)
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
        val packageName = app.data.packageName
        if (!app.isUsed() && !installing.contains(packageName) && !downloading.contains(packageName)) {
            apps.remove(packageName)
        }
    }
}
