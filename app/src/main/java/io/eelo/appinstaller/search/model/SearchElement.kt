package io.eelo.appinstaller.search.model

import android.content.Context
import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utils.Constants
import java.net.URL

class SearchElement(private val query: String, private val installManager: InstallManager, private val callback: SearchModelInterface) : AsyncTask<Context, Void, Void>() {

    val apps = ArrayList<Application>()
    private var nextPage = 1

    companion object {
        private val jsonReader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun loadMoreInBackground(context: Context) {
        execute(context)
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
        val url = URL(Constants.BASE_URL + "apps?action=search&keyword=" + query + "&page=" + nextPage + "&nres=" + Constants.RESULTS_PER_PAGE)
        val result = jsonReader.readValue<SearchResult>(url.openStream())
        val addingApps = result.createApplicationsList(context, installManager)
        apps.addAll(addingApps)
    }
}
