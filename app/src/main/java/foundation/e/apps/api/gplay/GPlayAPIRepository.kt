package foundation.e.apps.api.gplay

import javax.inject.Inject

class GPlayAPIRepository @Inject constructor(
    private val gPlayAPIImpl: GPlayAPIImpl
) {

    suspend fun fetchAuthData(): Unit? {
        return gPlayAPIImpl.fetchAuthData()
    }

    fun getSearchSuggestions(query: String) {
        return gPlayAPIImpl.getSearchSuggestions(query)
    }
}