package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application

interface SearchViewModelInterface {
    fun initialise(context: Context)

    fun getSuggestions(): MutableLiveData<ArrayList<String>>

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun onSearchQueryChanged(searchQuery: String)

    fun onSearchQuerySubmitted(searchQuery: String)
}
