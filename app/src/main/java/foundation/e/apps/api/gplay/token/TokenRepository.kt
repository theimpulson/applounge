package foundation.e.apps.api.gplay.token

import foundation.e.apps.api.gplay.token.data.Token
import retrofit2.Response
import javax.inject.Inject

class TokenRepository @Inject constructor(
    private val tokenInterface: TokenInterface
) {

    suspend fun getRandomToken(): Response<Token> {
        return tokenInterface.getRandomToken()
    }
}