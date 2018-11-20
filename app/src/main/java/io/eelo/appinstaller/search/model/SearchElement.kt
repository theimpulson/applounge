package io.eelo.appinstaller.search.model

import android.content.Context
import android.os.AsyncTask
import io.eelo.appinstaller.api.SearchRequest
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Common
import io.eelo.appinstaller.utils.Constants

class SearchElement(private val query: String, private val installManager: InstallManager, private val callback: SearchModelInterface) : AsyncTask<Context, Void, Void>() {

    val apps = ArrayList<Application>()
    private var nextPage = 1

    fun loadMoreInBackground(context: Context) {
        executeOnExecutor(Common.EXECUTOR, context)
    }

    override fun doInBackground(vararg params: Context): Void? {
        loadMore(params[0])
        nextPage++
        return null
    }

    override fun onPostExecute(result: Void?) {
        callback.onSearchComplete(apps)
    }

    private fun loadMore(context: Context) {
        val request = SearchRequest(query, nextPage, Constants.RESULTS_PER_PAGE).request()
        apps.addAll(request.getApplications(installManager, context))
    }
}
