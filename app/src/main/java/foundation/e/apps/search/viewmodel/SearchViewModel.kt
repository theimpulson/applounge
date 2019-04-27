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

package foundation.e.apps.search.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.search.model.SearchModel
import foundation.e.apps.utils.Error

class SearchViewModel : ViewModel(), SearchViewModelInterface {
    private val searchModel = SearchModel()

    override fun initialise(applicationManager: ApplicationManager) {
        searchModel.initialise(applicationManager)
    }

    override fun getSuggestions(): MutableLiveData<ArrayList<String>> {
        return searchModel.suggestionList
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return searchModel.applicationList
    }

    override fun getScreenError(): MutableLiveData<Error> {
        return searchModel.screenError
    }

    override fun onSearchQueryChanged(context: Context, searchQuery: String) {
        searchModel.searchSuggestions(context, searchQuery)
    }

    override fun onSearchQuerySubmitted(context: Context, searchQuery: String) {
        searchModel.screenError.value = null
        searchModel.search(context, searchQuery)
    }

    override fun loadMore(context: Context) {
        searchModel.loadMore(context)
    }
}
