package io.eelo.appinstaller.updates.model

import android.content.pm.PackageManager
import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.ApplicationData
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.application.model.State

class UnUpdatedAppsFinder(private val packageManager: PackageManager, private val callback: UpdatesModelInterface, private val installManager: InstallManager) : AsyncTask<Void, Void, Void>() {

    private var result :ArrayList<Application>? = null

    override fun doInBackground(vararg params: Void?): Void? {
        result = getNotUpdatedApplications()
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        callback.onAppsFound(this.result!!)
    }

    private fun getNotUpdatedApplications(): ArrayList<Application> {
        val result = ArrayList<Application>()
        getInstalledApplications().forEach { data ->
            val application = installManager.findOrCreateApp(data)
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
            result.add(ApplicationData(app.packageName, ""))
        }
        return result
    }

}
