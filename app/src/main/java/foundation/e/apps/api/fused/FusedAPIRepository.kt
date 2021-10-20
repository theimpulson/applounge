package foundation.e.apps.api.fused

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.cleanapk.CleanAPKInterface
import foundation.e.apps.api.cleanapk.data.app.Application
import foundation.e.apps.api.cleanapk.data.categories.Categories
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.data.Origin
import foundation.e.apps.api.data.SearchApp
import retrofit2.Response
import javax.inject.Inject

class FusedAPIRepository @Inject constructor(
    private val fusedAPIImpl: FusedAPIImpl
) {
    suspend fun getHomeScreenData(
    ): Response<HomeScreen> {
        return fusedAPIImpl.getHomeScreenData()
    }

    suspend fun getAppOrPWADetailsByID(
        id: String,
        architectures: List<String>? = null,
        type: String? = null
    ): Response<Application> {
        return fusedAPIImpl.getAppOrPWADetailsByID(id, architectures, type)
    }

    suspend fun getApplication(
        id: String,
        name: String,
        packageName: String,
        versionCode: Int,
        offerType: Int,
        authData: AuthData,
        origin: Origin
    ) {
        fusedAPIImpl.getApplication(
            id,
            name,
            packageName,
            versionCode,
            offerType,
            authData,
            origin
        )
    }

    suspend fun getCategoriesList(
        type: String = CleanAPKInterface.APP_TYPE_ANY,
        source: String = CleanAPKInterface.APP_SOURCE_ANY
    ): Response<Categories> {
        return fusedAPIImpl.getCategoriesList(type, source)
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry>? {
        return fusedAPIImpl.getSearchSuggestions(query, authData)
    }

    suspend fun fetchAuthData(): Unit? {
        return fusedAPIImpl.fetchAuthData()
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<SearchApp> {
        return fusedAPIImpl.getSearchResults(query, authData)
    }
}
