package foundation.e.apps.domain.repositories

import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.fused.data.FusedApp

interface IApplicationsRepository {
    suspend fun getOpenSourceApps(category: String): List<FusedApp>
    suspend fun getPWAApps(category: String): List<FusedApp>
    suspend fun getPlayStoreApps(browseUrl: String, authData: AuthData): List<FusedApp>
}