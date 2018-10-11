package io.eelo.appinstaller.search.model

import android.app.Activity
import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.application.model.Application
import io.eelo.appinstaller.application.model.InstallManager

class SearchModel : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
    private var element: SearchElement? = null
    private var installManager: InstallManager? = null
    private var activity: Activity? = null

    init {
        if (suggestionList.value == null) {
            suggestionList.value = ArrayList()
        }
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    override fun searchSuggestions(searchQuery: String) {
        Thread {
            // TODO search suggestions
            val suggestions = arrayListOf(searchQuery, searchQuery + "a", searchQuery + "b", searchQuery + "c")
            activity.run {
                suggestionList.value = suggestions
            }
        }.start()
    }

    override fun search(searchQuery: String) {
        element?.apps?.forEach { app ->
            app.decrementUses()
        }
        element = SearchElement(searchQuery, installManager!!)
        loadMore()
    }

    override fun loadMore() {
        Thread {
            element!!.search()
            val apps = element!!.apps
            activity!!.runOnUiThread {
                applicationList.value = apps
            }
        }.start()
    }
}
