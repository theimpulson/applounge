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
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.api.GitlabDataRequest
import foundation.e.apps.application.model.Application
import foundation.e.apps.application.model.ApplicationInfo
import foundation.e.apps.application.model.State
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

class UpdatesModel : UpdatesModelInterface {
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()
    private lateinit var context: Context
    var applicationManager: ApplicationManager? = null
    private var error: Error? = null

    override fun loadApplicationList(context: Context) {
        this.context = context
        if (Common.isNetworkAvailable(context)) {
            OutdatedApplicationsFileReader(context.packageManager, applicationManager!!, this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context)
        } else {
            screenError.value = Error.NO_INTERNET
        }

        Execute({
            var application: Application? = loadMicroGVersion()?.get(0)
            if (application!!.state != State.INSTALLED) {
                this.applicationList.postValue(loadMicroGVersion())
            }


        }, {
            if (error == null && this.applicationList.value != null) {
                val result = ArrayList<Application>()
                result.addAll(this.applicationList.value!!)
                if (this.applicationList.value!!.size != 0) {
                    this.applicationList.postValue(result)
                }
            } else {
                screenError.value = error
            }
        })
    }

    override fun onAppsFound(applications: ArrayList<Application>) {
        applicationList.value = applications
    }


    private fun loadMicroGVersion(): ArrayList<Application>? {
        var gitlabData: GitlabDataRequest.GitlabDataResult? = null
        GitlabDataRequest()
                .requestGmsCoreRelease { applicationError, listGitlabData ->

                    when (applicationError) {
                        null -> {
                            gitlabData = listGitlabData!!
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }
        return if (gitlabData != null) {
            gitlabData!!.getApplications(applicationManager!!, context)
        } else {
            null
        }
    }
}
