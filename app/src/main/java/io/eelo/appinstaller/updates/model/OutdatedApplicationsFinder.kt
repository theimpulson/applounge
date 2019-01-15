package io.eelo.appinstaller.updates.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.AsyncTask
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.application.model.State

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
            if (!isSystemApp(app.packageName)) {
                result.add(app.packageName)
            }
        }
        return result
    }

    private fun isSystemApp(packageName: String): Boolean {
        try {
            // Get package information for the app
            val appPackageInfo = packageManager.getPackageInfo(
                    packageName, PackageManager.GET_SIGNATURES)
            // Get application information for the app
            val appInfo = packageManager.getApplicationInfo(
                    packageName, 0)
            // Get package information for the Android system
            val systemPackageInfo = packageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES)

            // Compare app and Android system signatures
            if (appPackageInfo.signatures.isNotEmpty() &&
                    systemPackageInfo.signatures.isNotEmpty() &&
                    appPackageInfo.signatures[0] == systemPackageInfo.signatures[0]) {
                return true
            } else if (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or
                            ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                return true
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return false
    }
}
