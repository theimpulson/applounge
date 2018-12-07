package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Error

interface SearchViewModelInterface {
    fun initialise(installManager: InstallManager)

    fun getSuggestions(): MutableLiveData<ArrayList<String>>

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<Error>

    fun onSearchQueryChanged(context: Context, searchQuery: String)

    fun onSearchQuerySubmitted(context: Context, searchQuery: String)
}
