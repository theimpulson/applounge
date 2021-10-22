package foundation.e.apps.api.cleanapk.data.search

import foundation.e.apps.api.fused.data.SearchApp

data class Search(
    val apps: List<SearchApp>,
    val numberOfResults: Int,
    val pages: Int,
    val success: Boolean
)
