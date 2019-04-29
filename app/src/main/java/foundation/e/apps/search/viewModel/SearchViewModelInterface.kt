package foundation.e.apps.search.viewModel

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Error

interface SearchViewModelInterface {
    fun initialise(applicationManager: ApplicationManager)

    fun getSuggestions(): MutableLiveData<ArrayList<String>>

    fun getApplications(): MutableLiveData<ArrayList<Application>>

    fun getScreenError(): MutableLiveData<Error>

    fun onSearchQueryChanged(context: Context, searchQuery: String)

    fun onSearchQuerySubmitted(context: Context, searchQuery: String)

    fun loadMore(context: Context)
}
