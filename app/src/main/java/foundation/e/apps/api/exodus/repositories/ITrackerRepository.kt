package foundation.e.apps.api.exodus.repositories

import foundation.e.apps.api.Result
import foundation.e.apps.api.exodus.Tracker

interface ITrackerRepository {
    suspend fun getTrackerOfAnApp(appHandle: String): Result<List<Tracker>>
}