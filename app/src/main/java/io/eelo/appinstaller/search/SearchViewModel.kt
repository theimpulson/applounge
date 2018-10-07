package io.eelo.appinstaller.search

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Context
import io.eelo.appinstaller.application.Application

class SearchViewModel : ViewModel(), SearchViewModelInterface {
    private val searchModel = SearchModel()

    fun getSuggestions(): MutableLiveData<ArrayList<String>> {
        return searchModel.suggestionList
    }

    fun getApplications(): MutableLiveData<ArrayList<Application>> {
        return searchModel.applicationList
    }

    override fun onSearchQueryChanged(searchQuery: String) {
        searchModel.searchSuggestions(searchQuery)
    }

    override fun onSearchQuerySubmitted(searchQuery: String) {
        searchModel.search(searchQuery)
    }

    override fun onApplicationClick(context: Context, application: Application) {
        // TODO Show detailed view of application
    }

    override fun onInstallClick(context: Context, application: Application) {
        searchModel.install(context, application)
    }
}
