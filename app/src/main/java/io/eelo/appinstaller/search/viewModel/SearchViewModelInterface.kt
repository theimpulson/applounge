package io.eelo.appinstaller.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.ScreenError

interface SearchViewModelInterface {
    fun initialise(installManager: InstallManager)

    fun getSuggestions(): MutableLiveData<ArrayList<String>>

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<ScreenError>

    fun onSearchQueryChanged(searchQuery: String)

    fun onSearchQuerySubmitted(context: Context, searchQuery: String)
}
