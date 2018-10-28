package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.search.model.SearchModel

class SearchViewModel : ViewModel(), SearchViewModelInterface {
    private val searchModel = SearchModel()

    override fun initialise(installManager: InstallManager) {
        searchModel.initialise(installManager)
    }

    override fun getSuggestions(): MutableLiveData<ArrayList<String>> {
        return searchModel.suggestionList
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return searchModel.applicationList
    }

    override fun onSearchQueryChanged(searchQuery: String) {
        searchModel.searchSuggestions(searchQuery)
    }

    override fun onSearchQuerySubmitted(context: Context, searchQuery: String) {
        searchModel.search(context, searchQuery)
    }
}
