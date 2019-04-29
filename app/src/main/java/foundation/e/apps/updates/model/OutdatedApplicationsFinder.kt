package foundation.e.apps.updates.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.application.model.State
import foundation.e.apps.utils.Common

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
        val error = application.assertBasicData(context)
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
