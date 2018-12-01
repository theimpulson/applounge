package io.eelo.appinstaller.search.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.api.SearchRequest
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Constants

class SearchSuggestionsTask(private val searchQuery: String,
                            private val installManager: InstallManager,
                            private val callback: SearchModelInterface)
    : AsyncTask<Context, Void, ArrayList<String>>() {

    override fun doInBackground(vararg context: Context): ArrayList<String> {
        val searchRequest = SearchRequest(searchQuery, 1, Constants.SUGGESTIONS_RESULTS)
                .request()
        val applications = searchRequest.getApplications(installManager, context[0])
        val searchSuggestions = ArrayList<String>()
        applications.forEach {
            searchSuggestions.add(it.basicData!!.name)
        }
        return searchSuggestions
    }

    override fun onPostExecute(result: ArrayList<String>) {
        callback.onSearchSuggestionsRetrieved(result)
    }
}
