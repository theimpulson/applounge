package foundation.e.apps.api.gplay.token

import javax.inject.Inject

class TokenRepository @Inject constructor(
    private val tokenImpl: TokenImpl
)