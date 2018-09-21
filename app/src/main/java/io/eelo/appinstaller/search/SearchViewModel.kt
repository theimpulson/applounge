package io.eelo.appinstaller.search

import android.databinding.ObservableArrayList
import io.eelo.appinstaller.application.Application

class SearchViewModel : SearchViewModelInterface {
    val applicationList = ObservableArrayList<Application>()



    override fun onSearchClick(searchQuery: String) {
        // TODO Search for applications
    }

    override fun onApplicationClick(application: Application) {
        // TODO Show detailed view of application
    }

    override fun onInstallClick(application: Application) {
        // TODO Install APK
    }
}
