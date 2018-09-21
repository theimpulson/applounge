package io.eelo.appinstaller.search

import io.eelo.appinstaller.application.Application

interface SearchModelInterface {
    fun search(searchQuery: String)

    fun install(application: Application)
}