package foundation.e.apps.api.exodus.repositories

import foundation.e.apps.api.Result
import foundation.e.apps.api.exodus.models.AppPrivacyInfo

interface IAppPrivacyInfoRepository {
    suspend fun getAppPrivacyInfo(appHandle: String): Result<AppPrivacyInfo>
}
