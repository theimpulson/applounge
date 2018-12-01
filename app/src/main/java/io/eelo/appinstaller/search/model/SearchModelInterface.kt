package io.eelo.appinstaller.search.model

import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

interface SearchModelInterface {
    fun initialise(installManager: InstallManager)

    fun searchSuggestions(context: Context, searchQuery: String)

    fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>)

    fun search(context: Context, searchQuery: String)

    fun onSearchComplete(applicationList: ArrayList<Application>)

    fun loadMore(context: Context)
}
