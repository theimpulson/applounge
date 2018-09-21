package io.eelo.appinstaller.search

import io.eelo.appinstaller.application.Application

interface SearchViewModelInterface {
    fun onSearchClick(searchQuery: String)

    fun onApplicationClick(application: Application)

    fun onInstallClick(application: Application)
}
