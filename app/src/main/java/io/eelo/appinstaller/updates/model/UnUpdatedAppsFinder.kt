package io.eelo.appinstaller.updates.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.utils.Execute
import java.util.concurrent.atomic.AtomicInteger

class UnUpdatedAppsFinder(private val packageManager: PackageManager, private val callback: UpdatesModelInterface, private val installManager: InstallManager) : AsyncTask<Context, Void, Void>() {

    private var result: ArrayList<Application>? = null

    override fun doInBackground(vararg params: Context): Void? {
        result = getNotUpdatedApplications(params[0])
        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.onAppsFound(this.result!!)
    }

    private fun getNotUpdatedApplications(context: Context): ArrayList<Application> {
        val result = ArrayList<Application>()
        val installedApplications = getInstalledApplications()

        val waitingTasks = AtomicInteger(installedApplications.size)
        val blocker = Object()

        synchronized(blocker) {
            installedApplications.forEach { data ->
                val application = installManager.findOrCreateApp(context, data)
                Execute({
                    verifyApplication(application, waitingTasks, blocker, result, context)
                }, {})
            }
            blocker.wait()
        }
        return result
    }

    private fun verifyApplication(application: Application, waitingTasks: AtomicInteger, blocker: Object, apps: ArrayList<Application>, context: Context) {
        if (application.searchFullData(context) && application.state == State.NOT_UPDATED) {
            apps.add(application)
        } else {
            application.decrementUses()
        }
        if (waitingTasks.decrementAndGet() == 0) {
            synchronized(blocker) {
                blocker.notify()
            }
        }
    }

    private fun getInstalledApplications(): ArrayList<ApplicationData> {
        val result = ArrayList<ApplicationData>()
        packageManager.getInstalledApplications(0).forEach { app ->
            result.add(ApplicationData(app.packageName))
        }
        return result
    }

}
