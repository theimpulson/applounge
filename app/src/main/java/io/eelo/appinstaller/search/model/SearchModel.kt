package io.eelo.appinstaller.search.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Error

class SearchModel : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Error>()
    private var element: SearchElement? = null
    private var applicationManager: ApplicationManager? = null

    init {
        if (suggestionList.value == null) {
            suggestionList.value = ArrayList()
        }
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    override fun initialise(applicationManager: ApplicationManager) {
        this.applicationManager = applicationManager
    }

    override fun searchSuggestions(context: Context, searchQuery: String) {
        if (Common.isNetworkAvailable(context)) {
            SearchSuggestionsTask(searchQuery, applicationManager!!, this)
                    .executeOnExecutor(Common.EXECUTOR, context)
        }
    }

    override fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>) {
        this.suggestionList.value = suggestionsList
    }

    override fun search(context: Context, searchQuery: String) {
        if (Common.isNetworkAvailable(context)) {
            element?.apps?.forEach { app ->
                app.decrementUses()
            }
            element = SearchElement(searchQuery, applicationManager!!, this)
            loadMore(context)
        } else {
            applicationList.value = ArrayList()
            screenError.value = Error.NO_INTERNET
        }
    }

    override fun loadMore(context: Context) {
        element!!.loadMoreInBackground(context)
    }

    override fun onSearchComplete(error: Error?, applicationList: ArrayList<Application>) {
        if (error == null) {
            this.applicationList.value = applicationList
            if (applicationList.isEmpty()) {
                screenError.value = Error.NO_RESULTS
            }
        } else {
            screenError.value = error
        }
    }
}
