package io.eelo.appinstaller.search.model

import android.content.Context
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Error

interface SearchModelInterface {
    fun initialise(applicationManager: ApplicationManager)

    fun searchSuggestions(context: Context, searchQuery: String)

    fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>)

    fun search(context: Context, searchQuery: String)

    fun onSearchComplete(error: Error?, applicationList: ArrayList<Application>)

    fun loadMore(context: Context)
}
