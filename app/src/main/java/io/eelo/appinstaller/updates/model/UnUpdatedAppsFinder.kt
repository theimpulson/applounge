package io.eelo.appinstaller.updates.model

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.State

class UnUpdatedAppsFinder(private val packageManager: PackageManager, private val callback: UpdatesModelInterface, private val installManager: InstallManager) : AsyncTask<Context, Void, Void>() {

    private var result :ArrayList<Application>? = null

    override fun doInBackground(vararg params: Context): Void? {
        result = getNotUpdatedApplications(params[0])
        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.onAppsFound(this.result!!)
    }

    private fun getNotUpdatedApplications(context: Context): ArrayList<Application> {
        val result = ArrayList<Application>()
        getInstalledApplications().forEach { data ->
            val application = installManager.findOrCreateApp(context, data)
            application.searchFullData()
            if (application.state == State.NOT_UPDATED) {
                result.add(application)
            } else {
                application.decrementUses()
            }
        }
        return result
    }

    private fun getInstalledApplications(): ArrayList<ApplicationData> {
        val result = ArrayList<ApplicationData>()
        packageManager.getInstalledApplications(0).forEach { app ->
            result.add(ApplicationData(app.packageName))
        }
        return result
    }

}
