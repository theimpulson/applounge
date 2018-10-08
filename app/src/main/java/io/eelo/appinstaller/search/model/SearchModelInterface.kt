package io.eelo.appinstaller.search.model

interface SearchModelInterface {
    fun searchSuggestions(searchQuery: String)

    fun search(searchQuery: String)

    fun loadMore()
}
