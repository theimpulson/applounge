/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
import foundation.e.apps.utils.Constants

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
        var application: Application? = loadMicroGVersion(context)[0]
        if (application!!.state != State.INSTALLED) {
            result.add(application)
        }
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
            if (Common.isSystemApp(packageManager, app.packageName)) {
                if (app.packageName == Constants.MICROG_PACKAGE)
                    result.add(app.packageName)
            }
            if (!Common.isSystemApp(packageManager, app.packageName)) {
                result.add(app.packageName)
            }
        }
        return result
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
                           print("error")
                        }
                    }
                }
        return if (gitlabData != null) {
            gitlabData!!.getApplications(applicationManager!!, context)
        } else {
            emptyList()
        }
    }
}
