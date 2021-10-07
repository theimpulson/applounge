package foundation.e.apps.api.cleanapk.data.search

data class Search(
    val apps: List<App>,
    val numberOfResults: Int,
    val pages: Int,
    val success: Boolean
)