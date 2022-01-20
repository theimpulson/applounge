package foundation.e.apps.repositories

import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.fused.FusedAPIImpl
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.domain.repositories.IApplicationsRepository
import javax.inject.Inject

class ApplicationRepositoryImpl @Inject constructor(private val fusedAPIImpl: FusedAPIImpl) : IApplicationsRepository {
    override suspend fun getOpenSourceApps(category: String): List<FusedApp> {
        return fusedAPIImpl.getOpenSourceApps(category) ?: listOf()
    }

    override suspend fun getPWAApps(category: String): List<FusedApp> {
        return fusedAPIImpl.getPWAApps(category) ?: listOf()
    }

    override suspend fun getPlayStoreApps(browseUrl: String, authData: AuthData): List<FusedApp> {
        return fusedAPIImpl.getPlayStoreApps(browseUrl, authData)
    }
}