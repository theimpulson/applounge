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

package foundation.e.apps.search.model

import android.content.Context
import android.os.AsyncTask
import androidx.lifecycle.MutableLiveData
import foundation.e.apps.api.GitlabDataRequest
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Constants
import foundation.e.apps.utils.Error
import foundation.e.apps.utils.Execute

class SearchModel : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()
    lateinit var applicationManager: ApplicationManager
    private var pageNumber = 0
    private lateinit var searchQuery: String
    private lateinit var context: Context
    private var error: Error? = null

    override fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun searchSuggestions(context: Context, searchQuery: String) {
        this.searchQuery = searchQuery
        this.context = context
        if (searchQuery.length >= Constants.MIN_SEARCH_TERM_LENGTH) {
            if (Common.isNetworkAvailable(context)) {
                SearchSuggestionsTask(searchQuery, applicationManager!!, this)
                        .executeOnExecutor(Common.EXECUTOR, context)
            }
        } else {
            suggestionList.value = null
        }
    }

    override fun onSearchSuggestionsRetrieved(
            searchTerm: String,
            suggestionsList: ArrayList<String>) {
        if (searchTerm == searchQuery) {
            this.suggestionList.value = suggestionsList
        }
    }

    override fun search(context: Context, searchQuery: String) {
        pageNumber = 0
        this.searchQuery = searchQuery
        applicationList.value?.forEach { app ->
            app.decrementUses()
        }
        loadMore(context)
    }

    override fun loadMore(context: Context) {
        if (Common.isNetworkAvailable(context)) {
            pageNumber++
            SearchElement(searchQuery, pageNumber, applicationManager!!, this)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, context)
        } else {
            screenError.value = Error.NO_INTERNET
        }
    }

    override fun onSearchComplete(error: Error?, applicationList: ArrayList<Application>) {

        if (error == null) {
            if ("microG Exposure Notification version".contains(searchQuery, true)) {
                Execute({
                    this.applicationList.postValue(loadMicroGVersion())
                }, {
                    if (error == null &&   this.applicationList.value != null) {
                        val result = ArrayList<Application>()
                        result.addAll(this.applicationList.value!!)
                        if (  this.applicationList.value!!.size != 0) {
                            this.applicationList.postValue(result)
                        }
                    } else {
                        screenError.value = error
                    }
                })
            } else {
                if (applicationList.isNotEmpty()) {
                    if (pageNumber > 1 && this.applicationList.value != null) {
                        val combinedAppList = this.applicationList.value!!
                        combinedAppList.addAll(applicationList)
                        this.applicationList.value = combinedAppList
                    } else {
                        this.applicationList.value = applicationList
                    }
                } else {
                    screenError.value = Error.NO_RESULTS
                }
            }
        } else {
            screenError.value = error
        }
    }

/*gets microG application from gitlab*/
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
            gitlabData!!.getApplications(applicationManager, context)
        } else {
            null
        }
    }
}
