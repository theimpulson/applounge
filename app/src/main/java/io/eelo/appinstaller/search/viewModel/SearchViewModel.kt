package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.search.model.SearchModel

class SearchViewModel : ViewModel(), SearchViewModelInterface {
    private val searchModel = SearchModel()

    override fun getSuggestions(): MutableLiveData<ArrayList<String>> {
        return searchModel.suggestionList
    }

    override fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return searchModel.applicationList
    }

    override fun onSearchQueryChanged(searchQuery: String) {
        searchModel.searchSuggestions(searchQuery)
    }

    override fun onSearchQuerySubmitted(searchQuery: String) {
        searchModel.search(searchQuery)
    }
}
