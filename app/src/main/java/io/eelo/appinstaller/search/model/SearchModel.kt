package io.eelo.appinstaller.search.model

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

class SearchModel : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
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

    override fun initialise(context: Context) {
        if (installManager == null) {
            installManager = InstallManager(context)
        }
    }

    override fun searchSuggestions(searchQuery: String) {
        val suggestions = arrayListOf(searchQuery, searchQuery + "a", searchQuery + "b", searchQuery + "c")
        onSearchSuggestionsRetrieved(suggestions)
    }

    override fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>) {
        this.suggestionList.value = suggestionsList
    }

    override fun search(searchQuery: String) {
        element?.apps?.forEach { app ->
            app.decrementUses()
        }
        element = SearchElement(searchQuery, installManager!!, this)
        loadMore()
    }

    override fun loadMore() {
        element!!.loadMoreInBackground()
    }

    override fun onSearchComplete(applicationList: ArrayList<Application>) {
        this.applicationList.value = applicationList
    }
}
