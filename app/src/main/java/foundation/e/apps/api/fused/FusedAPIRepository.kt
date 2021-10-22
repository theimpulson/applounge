package foundation.e.apps.api.fused

import com.aurora.gplayapi.SearchSuggestEntry
import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.cleanapk.data.home.HomeScreen
import foundation.e.apps.api.fused.data.CategoryApp
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.SearchApp
import retrofit2.Response
import javax.inject.Inject

class FusedAPIRepository @Inject constructor(
    private val fusedAPIImpl: FusedAPIImpl
) {
    suspend fun getHomeScreenData(): Response<HomeScreen> {
        return fusedAPIImpl.getHomeScreenData()
    }

    suspend fun getApplicationDetails(
        id: String,
        packageName: String,
        authData: AuthData,
        origin: Origin
    ): FusedApp? {
        return fusedAPIImpl.getApplicationDetails(id, packageName, authData, origin)
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

    suspend fun getCategoriesList(listType: String): List<CategoryApp> {
        return fusedAPIImpl.getCategoriesList(listType)
    }

    suspend fun getSearchSuggestions(query: String, authData: AuthData): List<SearchSuggestEntry> {
        return fusedAPIImpl.getSearchSuggestions(query, authData)
    }

    suspend fun fetchAuthData(): Unit? {
        return fusedAPIImpl.fetchAuthData()
    }

    suspend fun getSearchResults(query: String, authData: AuthData): List<SearchApp> {
        return fusedAPIImpl.getSearchResults(query, authData)
    }

    suspend fun listApps(category: String): List<SearchApp>? {
        return fusedAPIImpl.listApps(category)
    }
}
