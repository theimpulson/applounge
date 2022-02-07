package foundation.e.apps.api.exodus

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ExodusTrackerApi {

    companion object {
        const val BASE_URL = "https://exodus.ecloud.global/api/"
        const val VERSION = "190239"
    }

    @GET("trackers?v=$VERSION")
    suspend fun getTrackerList(): Response<Trackers>

    @GET("search/{appHandle}")
    suspend fun getTrackerInfoOfApp(@Path("appHandle") appHandle: String): Response<Map<String, TrackerInfo>>
}
