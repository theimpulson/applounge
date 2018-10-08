package io.eelo.appinstaller.search.model

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.Settings
import io.eelo.appinstaller.application.Application

class SearchModel(private val settings: Settings) : SearchModelInterface {

    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()
    private var element: SearchElement? = null

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
            suggestionList.value = arrayListOf(searchQuery, searchQuery + "a", searchQuery + "b", searchQuery + "c")
        }.start()
    }

    override fun search(searchQuery: String) {
        element?.apps?.forEach {
            app -> app.decrementUses()
        }
        element = SearchElement(searchQuery, settings)
        loadMore()
    }

    override fun loadMore() {
        Thread {
            element!!.search()
            applicationList.value = element!!.apps
        }.start()
    }
}
