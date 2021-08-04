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

import android.content.Context
import android.content.pm.PackageManager
import android.os.AsyncTask
import foundation.e.apps.api.GitlabDataRequest
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.State
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants.MICROG_SHARED_PREF
import foundation.e.apps.utils.PreferenceStorage

class OutdatedApplicationsFileReader(
    private val packageManager: PackageManager,
    private val applicationManager: ApplicationManager,
    private val callback: UpdatesModelInterface
) :
    AsyncTask<Context, Void, ArrayList<Application>>() {
    override fun doInBackground(vararg context: Context): ArrayList<Application> {
        val applications = ArrayList<Application>()
        val application: Application = loadMicroGVersion(context[0])[0]
        if (PreferenceStorage(context[0])
            .getBoolean(MICROG_SHARED_PREF, false) &&
            application.state == State.NOT_UPDATED
        ) {
            applications.addAll(loadMicroGVersion(context[0]))
        }
        try {
            val installedApplications = getInstalledApplications()
            installedApplications.forEach { packageName ->
                val app = applicationManager.findOrCreateApp(packageName)
                verifyApplication(app, applications, context)
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        return applications
    }

    override fun onPostExecute(result: ArrayList<Application>) {
        callback.onAppsFound(result)
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

    private fun verifyApplication(
        application: Application,
        apps: ArrayList<Application>,
        context: Array<out Context>
    ) {
        val error = application.assertBasicData(context[0])
        if (error == null && application.state == State.NOT_UPDATED) {
            apps.add(application)
        } else {
            application.decrementUses()
        }
    }

    private fun loadMicroGVersion(context: Context): List<Application> {
        var gitlabData: GitlabDataRequest.GitlabDataResult? = null
        GitlabDataRequest()
            .requestGmsCoreRelease { applicationError, listGitlabData ->

                when (applicationError) {
                    null -> {
                        gitlabData = listGitlabData!!
                    }
                    else -> {
                        print("error occurred")
                    }
                }
            }
        return if (gitlabData != null) {
            gitlabData!!.getApplications(applicationManager, context)
        } else {
            emptyList()
        }
    }
}
