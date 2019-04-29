package foundation.e.apps.applicationmanager

import android.content.Context
import foundation.e.apps.application.model.Application
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

class ApplicationManager {

    private val apps = HashMap<String, Application>()
    private val queue = LinkedBlockingQueue<Application>()

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
    fun install(context: Context, app: Application) {
        if (!queue.contains(app)) {
            queue.put(app)
            queue.put(app)
        }
        app.checkForStateUpdate(context)
    }

    fun start(context: Context) {
        Thread {
            startInstalls(context)
        }.start()
    }

    private fun startInstalls(context: Context) {
        while (true) {
            val app = queue.take()
            app.download(context)
            stopInstalling(context, app)
            tryRemove(app)
        }
    }

    fun tryRemove(app: Application) {
        if (!app.isUsed() && !queue.contains(app)) {
            apps.remove(app.packageName)
        }
    }

    @Synchronized
    fun stopInstalling(context: Context, app: Application) {
        while (queue.remove(app)) {
        }
        app.checkForStateUpdate(context)
    }

    fun isInstalling(app: Application): Boolean {
        return queue.contains(app)
    }
}
