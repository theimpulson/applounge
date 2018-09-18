package io.eelo.appinstaller.search

import android.databinding.ObservableArrayList
import io.eelo.appinstaller.application.Application

class SearchViewModel : SearchViewModelInterface {
    val applicationList = ObservableArrayList<Application>()

    override fun search(searchQuery: String) {
        // TODO Search for applications
    }

    override fun install(application: Application) {
        // TODO Install APK
    }
}
