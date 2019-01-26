package foundation.e.apps.search.model

import android.content.Context
import foundation.e.apps.application.model.Application
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Error

interface SearchModelInterface {
    fun initialise(applicationManager: ApplicationManager)

    fun searchSuggestions(context: Context, searchQuery: String)

    fun onSearchSuggestionsRetrieved(suggestionsList: ArrayList<String>)

    fun search(context: Context, searchQuery: String)

    fun onSearchComplete(error: Error?, applicationList: ArrayList<Application>)

    fun loadMore(context: Context)
}
