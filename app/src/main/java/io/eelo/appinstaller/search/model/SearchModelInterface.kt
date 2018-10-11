package io.eelo.appinstaller.search.model

import android.content.Context
import io.eelo.appinstaller.application.model.Application

interface SearchModelInterface {
    fun initialise(context: Context)

    fun searchSuggestions(searchQuery: String)

    fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>)

    fun search(searchQuery: String)

    fun onSearchComplete(applicationList: ArrayList<Application>)

    fun loadMore()
}
