package io.eelo.appinstaller.updates.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.application.model.State
import io.eelo.appinstaller.utils.Common

class OutdatedApplicationsFinder(private val packageManager: PackageManager,
                                 private val callback: UpdatesWorkerInterface,
                                 private val applicationManager: ApplicationManager) :
        AsyncTask<Context, Any, Any>() {

    private var result: ArrayList<Application>? = null

    override fun doInBackground(vararg params: Context): Any? {
        result = getOutdatedApplications(params[0])
        return null
    }

    override fun onPostExecute(result: Any?) {
        callback.onApplicationsFound(this.result!!)
    }

    private fun getOutdatedApplications(context: Context): ArrayList<Application> {
        val result = ArrayList<Application>()
        val installedApplications = getInstalledApplications()
        installedApplications.forEach { packageName ->
            val application = applicationManager.findOrCreateApp(packageName)
            verifyApplication(application, result, context)
        }
        return result
    }

    private fun verifyApplication(application: Application, apps: ArrayList<Application>,
                                  context: Context) {
        val error = application.assertFullData(context)
        if (error == null && application.state == State.NOT_UPDATED) {
            apps.add(application)
        } else {
            application.decrementUses()
        }
    }

    private fun getInstalledApplications(): ArrayList<String> {
        val result = ArrayList<String>()
        packageManager.getInstalledApplications(0).forEach { app ->
            if (!Common.isSystemApp(packageManager, app.packageName)) {
                result.add(app.packageName)
            }
        }
        return result
    }
}
