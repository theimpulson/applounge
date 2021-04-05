/*
 * Copyright (C) 2019-2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.updates.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import foundation.e.apps.R
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.State
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.updates.UpdatesNotifier
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class UpdatesWorker(context: Context, params: WorkerParameters) : Worker(context, params),
        UpdatesWorkerInterface {
    private val TAG = "UpdatesWorker"
    private val blocker = Object()
    private var notifyAvailable = true
    private var installAutomatically = true
    private var wifiOnly = false
    val applicationManager = ApplicationManager()
    private var error: Error? = null

    override fun doWork(): Result {
        Log.i(TAG, "Checking for app updates")
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
                        R.string.pref_update_wifi_only_key), true)
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
            val isConnectedToUnmeteredNetwork = isConnectedToUnmeteredNetwork(applicationContext)
            if (notifyAvailable) {
                UpdatesNotifier().showNotification(
                        applicationContext,
                        applications.size,
                        installAutomatically,
                        wifiOnly,
                        isConnectedToUnmeteredNetwork)
            }
            if (installAutomatically && canWriteStorage(applicationContext)) {
                if (wifiOnly) {
                    if (isConnectedToUnmeteredNetwork) {
                        applications.forEach {
                            if (it.packageName == Constants.MICROG_PACKAGE) {
                                it.buttonClicked(applicationContext, null)
                            }
                            if (it.state == State.NOT_UPDATED) {
                                Log.i(TAG, "Updating ${it.packageName}")
                                it.buttonClicked(applicationContext, null)
                            }

                        }
                    }
                } else {
                    applications.forEach {
                        if (it.packageName == Constants.MICROG_PACKAGE) {
                            it.buttonClicked(applicationContext, null)
                        }
                        if (it.state == State.NOT_UPDATED) {
                            Log.i(TAG, "Updating ${it.packageName}")
                            it.buttonClicked(applicationContext, null)
                        }
                    }
                }
            }
        }
        synchronized(blocker) {
            blocker.notify()
        }
    }

    private fun canWriteStorage(context: Context) = !(android.os.Build.VERSION.SDK_INT >= 23 &&
            context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
            PackageManager.PERMISSION_GRANTED)

    private fun isConnectedToUnmeteredNetwork(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
        return if (connectivityManager is ConnectivityManager) {
            val networkInfo: NetworkInfo? = connectivityManager.activeNetworkInfo
            networkInfo?.isConnected ?: false
        } else false
    }
}
