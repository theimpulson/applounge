package foundation.e.apps.api.gplay

import com.aurora.gplayapi.data.models.AuthData
import javax.inject.Inject

class GPlayAPIRepository @Inject constructor(
    private val gPlayAPIImpl: GPlayAPIImpl
) {

    suspend fun fetchAuthData(): Unit? {
        return gPlayAPIImpl.fetchAuthData()
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData) {
        return gPlayAPIImpl.getSearchSuggestions(query, authData)
    }
}