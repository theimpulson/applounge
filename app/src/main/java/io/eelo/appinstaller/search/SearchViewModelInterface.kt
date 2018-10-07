package io.eelo.appinstaller.search

import android.content.Context
import io.eelo.appinstaller.application.Application

interface SearchViewModelInterface {
    fun onSearchQueryChanged(searchQuery: String)

    fun onSearchQuerySubmitted(searchQuery: String)

    fun onApplicationClick(context: Context, application: Application)

    fun onInstallClick(context:Context, application: Application)
}
