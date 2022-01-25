package foundation.e.apps.api.exodus.repositories

interface ITrackerRepository {
    suspend fun getTrackerList(): Result<Boolean>
}