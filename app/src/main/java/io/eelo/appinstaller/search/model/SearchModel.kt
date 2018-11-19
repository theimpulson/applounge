package io.eelo.appinstaller.search.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants

class SearchModel : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
    var screenError = MutableLiveData<Int>()
    private var element: SearchElement? = null
    private var installManager: InstallManager? = null

    init {
        if (suggestionList.value == null) {
            suggestionList.value = ArrayList()
        }
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    override fun initialise(installManager: InstallManager) {
        this.installManager = installManager
    }

    override fun searchSuggestions(searchQuery: String) {
        val suggestions = arrayListOf(searchQuery, searchQuery + "a", searchQuery + "b", searchQuery + "c")
        onSearchSuggestionsRetrieved(suggestions)
    }

    override fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>) {
        this.suggestionList.value = suggestionsList
    }

    override fun search(context: Context, searchQuery: String) {
        if (Common.isNetworkAvailable(context)) {
            element?.apps?.forEach { app ->
                app.decrementUses()
            }
            element = SearchElement(searchQuery, installManager!!, this)
            loadMore(context)
        } else {
            screenError.value = Constants.ERROR_NO_INTERNET
        }
    }

    override fun loadMore(context: Context) {
        element!!.loadMoreInBackground(context)
    }

    override fun onSearchComplete(applicationList: ArrayList<Application>) {
        this.applicationList.value = applicationList
    }
}
