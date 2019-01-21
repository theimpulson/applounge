package io.eelo.appinstaller.updates.model

import android.content.Context
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.eelo.appinstaller.R
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.updates.UpdatesNotifier
import io.eelo.appinstaller.utils.Constants

class UpdatesWorker(context: Context, params: WorkerParameters) : Worker(context, params),
        UpdatesWorkerInterface {
    private val TAG = "UpdatesWorker"
    private val blocker = Object()
    private var notifyAvailable = true
    private var installAutomatically = true
    private var wifiOnly = false

    override fun doWork(): Result {
        Log.i(TAG, "Checking for app updates")
        val applicationManager = ApplicationManager()
        applicationManager.start(applicationContext)
        loadOutdatedApplications(applicationManager)
        Log.i(TAG, "Ids of apps with pending updates written to file")
        return Result.success()
    }

    private fun loadPreferences() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        notifyAvailable =
                preferences.getBoolean(applicationContext.getString(
                        R.string.pref_update_notify_key), true)
        installAutomatically =
                preferences.getBoolean(applicationContext.getString(
                        R.string.pref_update_install_automatically_key), true)
        wifiOnly =
                preferences.getBoolean(applicationContext.getString(
                        R.string.pref_update_wifi_only_key), false)
    }

    private fun loadOutdatedApplications(applicationManager: ApplicationManager) {
        OutdatedApplicationsFinder(applicationContext.packageManager, this,
                applicationManager).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
                applicationContext)
        synchronized(blocker) {
            blocker.wait()
        }
    }

    override fun onApplicationsFound(applications: ArrayList<Application>) {
        Log.i(TAG, "${applications.size} app updates found")
        if (applications.size > 0) {
            loadPreferences()
            applicationContext.openFileOutput(Constants.OUTDATED_APPLICATIONS_FILENAME,
                    Context.MODE_PRIVATE).use {
                applications.forEach { application ->
                    it.write((application.basicData!!.packageName + "\n").toByteArray())
                }
                it.close()
            }
            if (notifyAvailable) {
                UpdatesNotifier().showNotification(
                        applicationContext,
                        applications.size,
                        installAutomatically)
            }
        }
        synchronized(blocker) {
            blocker.notify()
        }
    }
}
