package io.eelo.appinstaller.search.viewModel

interface SearchViewModelInterface {
    fun onSearchQueryChanged(searchQuery: String)

    fun onSearchQuerySubmitted(searchQuery: String)
}
