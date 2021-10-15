package foundation.e.apps.api.gplay.token

import com.aurora.gplayapi.data.models.AuthData
import javax.inject.Inject

class TokenRepository @Inject constructor(
    private val tokenImpl: TokenImpl
) {

    suspend fun getAuthData(): AuthData? {
        return tokenImpl.getAuthData()
    }
}
