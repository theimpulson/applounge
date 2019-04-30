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

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Common
import foundation.e.apps.utils.Error

class SearchModel : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()
    private var applicationManager: ApplicationManager? = null
    private var pageNumber = 0
    private lateinit var searchQuery: String

    override fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun searchSuggestions(context: Context, searchQuery: String) {
        if (Common.isNetworkAvailable(context)) {
            SearchSuggestionsTask(searchQuery, applicationManager!!, this)
                    .executeOnExecutor(Common.EXECUTOR, context)
        }
    }

    override fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>) {
        this.suggestionList.value = suggestionsList
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
        } else {
            screenError.value = error
        }
    }
}
