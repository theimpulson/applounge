package foundation.e.apps.api.exodus.repositories

import foundation.e.apps.api.exodus.ExodusTrackerApi
import foundation.e.apps.api.getResult
import javax.inject.Inject

class TrackerRepositoryImpl @Inject constructor(private val exodusTrackerApi: ExodusTrackerApi) : ITrackerRepository {
    override suspend fun getTrackerList(): Result<Boolean> {
        val result = getResult { exodusTrackerApi.getTrackerList() }
        return Result.success(result.isSuccess())
    }
}