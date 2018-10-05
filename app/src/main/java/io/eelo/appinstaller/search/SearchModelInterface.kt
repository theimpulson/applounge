package io.eelo.appinstaller.search

import io.eelo.appinstaller.application.Application

interface SearchModelInterface {
    fun searchSuggestions(searchQuery: String)

    fun search(searchQuery: String)

    fun install(application: Application)
}
