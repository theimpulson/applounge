package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.search.model.SearchModel
import io.eelo.appinstaller.utils.Error

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
