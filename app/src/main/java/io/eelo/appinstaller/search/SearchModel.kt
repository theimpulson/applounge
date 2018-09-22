package io.eelo.appinstaller.search

import io.eelo.appinstaller.Settings

class SearchModel(private val settings: Settings) {
    private val searchElements = ArrayList<SearchElement>()

    fun search(query: String): SearchElement {
        val element = SearchElement(query, settings)
        searchElements.add(element)
        element.search()
        return element
    }

    fun loadMore() {
        searchElements.last().search()
    }

    fun back(): SearchElement? {
        if (searchElements.isEmpty()) {
            return null
        }
        searchElements.removeAt(searchElements.lastIndex)
        return searchElements.last()
    }
}
