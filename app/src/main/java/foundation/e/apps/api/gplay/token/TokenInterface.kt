package foundation.e.apps.api.gplay.token

import foundation.e.apps.api.gplay.token.data.Token
import retrofit2.Response
import retrofit2.http.GET

interface TokenInterface {

    // TODO: Implement specific-user and device token functions

    companion object {
        const val BASE_URL = "https://eu.gtoken.ecloud.global"
    }

    @GET(BASE_URL)
    suspend fun getRandomToken(): Response<Token>
}