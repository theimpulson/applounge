package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

interface SearchViewModelInterface {
    fun initialise(installManager: InstallManager)

    fun getSuggestions(): MutableLiveData<ArrayList<String>>

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun onSearchQueryChanged(searchQuery: String)

    fun onSearchQuerySubmitted(searchQuery: String)
}
