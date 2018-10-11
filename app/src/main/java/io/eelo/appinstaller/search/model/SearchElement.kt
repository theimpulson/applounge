package io.eelo.appinstaller.search.model

import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utlis.Constants
import java.net.URL

class SearchElement(private val query: String, private val installManager: InstallManager, private val callback: SearchModelInterface) : AsyncTask<Void, Void, Void>() {

    val apps = ArrayList<Application>()
    private var nextPage = 1

    companion object {
        private val jsonReader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    fun loadMoreInBackground() {
        execute()
    }

    override fun doInBackground(vararg params: Void?): Void? {
        loadMore()
        nextPage++
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        callback.onSearchComplete(apps)
    }

    private fun loadMore() {
        val url = URL(Constants.BASE_URL + "apps?action=search&keyword=" + query + "&page=" + nextPage + "&nres=" + Constants.RESULTS_PER_PAGE)
        val result = jsonReader.readValue<SearchResult>(url.openStream())
        val addingApps = result.createApplicationsList(installManager)
        apps.addAll(addingApps)
    }
}
