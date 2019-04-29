package foundation.e.apps.search.model

import android.content.Context
import android.os.AsyncTask
import foundation.e.apps.api.SearchRequest
import foundation.e.apps.applicationmanager.ApplicationManager
import foundation.e.apps.utils.Constants

class SearchSuggestionsTask(private val searchQuery: String,
                            private val applicationManager: ApplicationManager,
                            private val callback: SearchModelInterface)
    : AsyncTask<Context, Void, ArrayList<String>>() {

    override fun doInBackground(vararg context: Context): ArrayList<String> {
        val searchSuggestions = ArrayList<String>()
        SearchRequest(searchQuery, 1, Constants.SUGGESTIONS_RESULTS)
                .request { applicationError, searchResult ->
                    when (applicationError) {
                        null -> {
                            val applications = searchResult!!.getApplications(applicationManager, context[0])
                            applications.forEach {
                                searchSuggestions.add(it.basicData!!.name)
                            }
                        }
                        else -> {
                            // Do nothing
                        }
                    }
                }
        return searchSuggestions
    }

    override fun onPostExecute(result: ArrayList<String>) {
        callback.onSearchSuggestionsRetrieved(result)
    }
}
