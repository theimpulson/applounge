package io.eelo.appinstaller.search.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.api.SearchRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.applicationmanager.ApplicationManager
import io.eelo.appinstaller.utils.Error
import io.eelo.appinstaller.utils.Constants

class SearchElement(private val query: String, private val pageNumber: Int,
                    private val applicationManager: ApplicationManager,
                    private val callback: SearchModelInterface) :
        AsyncTask<Context, Void, ArrayList<Application>>() {
    private var error: Error? = null

    override fun doInBackground(vararg params: Context): ArrayList<Application> {
        val apps = ArrayList<Application>()
        SearchRequest(query, pageNumber, Constants.RESULTS_PER_PAGE)
                .request { applicationError, searchResult ->
                    when (applicationError) {
                        null -> {
                            apps.addAll(searchResult!!.getApplications(applicationManager, params[0]))
                        }
                        else -> {
                            error = applicationError
                        }
                    }
                }
        return apps
    }

    override fun onPostExecute(result: ArrayList<Application>) {
        callback.onSearchComplete(error, result)
    }
}
