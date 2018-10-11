package io.eelo.appinstaller.search.model

import android.os.AsyncTask
import com.fasterxml.jackson.databind.ObjectMapper
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager
import io.eelo.appinstaller.utlis.Constants
import java.io.IOException
import java.net.URL
import kotlin.collections.ArrayList

class SearchElement(private val query: String, private val installManager: InstallManager, private val callback: SearchModelInterface) {

    val apps = ArrayList<Application>()
    private var nextPage = 0

    companion object {
        private val jsonReader = ObjectMapper().readerFor(SearchResult::class.java)
    }

    @Throws(IOException::class)
    fun search() {
        SearchTask(nextPage, apps, query, installManager, callback).execute()
    }

    class SearchTask(private var page: Int, private val apps: ArrayList<Application>, private val query: String, private val installManager: InstallManager, private val callback: SearchModelInterface) : AsyncTask<Void, Void, Void?>() {

        override fun doInBackground(vararg p0: Void?): Void? {
            val url = URL(Constants.BASE_URL + "apps?action=search&keyword=" + query + "&page=" + page + "&nres=" + Constants.RESULTS_PER_PAGE)
            val result = jsonReader.readValue<SearchResult>(url.openStream())
            val addingApps = result.createApplicationsList(installManager)
            apps.addAll(addingApps)
            page++
            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            callback.onSearchComplete(apps)
        }
    }
}
