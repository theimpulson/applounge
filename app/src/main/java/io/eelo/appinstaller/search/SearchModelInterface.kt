package io.eelo.appinstaller.search

import android.content.Context
import io.eelo.appinstaller.application.Application

interface SearchModelInterface {
    fun searchSuggestions(searchQuery: String)

    fun search(searchQuery: String)

    fun install(context: Context, application: Application)
}
