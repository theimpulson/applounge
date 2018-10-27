package io.eelo.appinstaller.search.model

import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

interface SearchModelInterface {
    fun initialise(installManager: InstallManager)

    fun searchSuggestions(searchQuery: String)

    fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>)

    fun search(searchQuery: String)

    fun onSearchComplete(applicationList: ArrayList<Application>)

    fun loadMore()
}
