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

package foundation.e.apps.search.model

import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.api.AllAppsSearchRequest
import foundation.e.apps.api.GitlabDataRequest
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error

class SearchElement(private val query: String, private val pageNumber: Int,
                    private val applicationManager: ApplicationManager,
                    private val callback: SearchModelInterface) :
        AsyncTask<Context, Void, ArrayList<Application>>() {
    private var error: Error? = null

    override fun doInBackground(vararg params: Context): ArrayList<Application> {
        val apps = ArrayList<Application>()
        if ("microG Exposure Notification version".contains(query, true)) {
                apps.addAll(loadMicroGVersion(params[0]))
        }

        AllAppsSearchRequest(query, pageNumber, Constants.RESULTS_PER_PAGE)
                .request { applicationError, searchResult ->
                    when (applicationError) {
                        null -> {
                            apps.addAll(searchResult!!.getApplications(applicationManager, params[0]))
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }

        return apps
    }

    override fun onPostExecute(result: ArrayList<Application>) {
        callback.onSearchComplete(error, result)
    }

    /*gets microG application from gitlab*/
    private fun loadMicroGVersion(context: Context): List<Application> {
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
            gitlabData!!.getApplications(applicationManager, context)
        } else {
            emptyList()
        }
    }
}
