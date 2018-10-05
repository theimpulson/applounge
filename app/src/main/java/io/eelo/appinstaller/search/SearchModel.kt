package io.eelo.appinstaller.search

import android.arch.lifecycle.MutableLiveData
import io.eelo.appinstaller.application.Application

class SearchModel : SearchModelInterface {
    val suggestionList = MutableLiveData<ArrayList<String>>()
    val applicationList = MutableLiveData<ArrayList<Application>>()

    init {
        if (suggestionList.value == null) {
            suggestionList.value = ArrayList()
        }
        if (applicationList.value == null) {
            applicationList.value = ArrayList()
        }
    }

    override fun searchSuggestions(searchQuery: String) {
        // TODO Get search query suggestions
    }

    override fun search(searchQuery: String) {
        // TODO Search for applications
        // Jo Please start your search from here.
        // Once the search is complete and you receive a callback,
        // update the applicationList variable (applicationList.value).
    }

    override fun install(application: Application) {
        // TODO Install APK
    }
}
