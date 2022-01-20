package foundation.e.apps.domain

import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.domain.repositories.IApplicationsRepository
import javax.inject.Inject

class ApplicationListUseCase @Inject constructor(private val applicationRepository: IApplicationsRepository) {
    suspend fun getAppsList(category: String, browseUrl: String, authData: AuthData, source: String): List<FusedApp> {
        return when(source) {
            "Open Source" -> applicationRepository.getOpenSourceApps(category)
            "PWA" -> applicationRepository.getPWAApps(category)
            else -> applicationRepository.getPlayStoreApps(browseUrl, authData)
        }
    }
}