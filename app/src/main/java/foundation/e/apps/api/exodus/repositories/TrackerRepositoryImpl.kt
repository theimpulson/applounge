package foundation.e.apps.api.exodus.repositories

import foundation.e.apps.api.Result
import foundation.e.apps.api.exodus.ExodusTrackerApi
import foundation.e.apps.api.exodus.Tracker
import foundation.e.apps.api.exodus.TrackerDao
import foundation.e.apps.api.exodus.TrackerInfo
import foundation.e.apps.api.getResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TrackerRepositoryImpl @Inject constructor(
    private val exodusTrackerApi: ExodusTrackerApi,
    private val trackerDao: TrackerDao
) : ITrackerRepository {
    private var trackers: List<Tracker> = listOf()

    override suspend fun getTrackersOfAnApp(appHandle: String): Result<List<Tracker>> {
        val appTrackerInfoResult = getResult { exodusTrackerApi.getTrackerInfoOfApp(appHandle) }
        if (appTrackerInfoResult.isSuccess()) {
            return handleAppTrackerInfoResultSuccess(appTrackerInfoResult, appHandle)
        }
        return Result.error(extractErrorMessage(appTrackerInfoResult))
    }

    private suspend fun handleAppTrackerInfoResultSuccess(
        appTrackerResult: Result<Map<String, TrackerInfo>>,
        appHandle: String
    ): Result<List<Tracker>> {
        if(trackers.isEmpty()) {
            generateTrackerList()
        }
        return createAppTrackerListResult(appTrackerResult, appHandle)
    }

    private suspend fun generateTrackerList() {
        val trackerListOfLocalDB = trackerDao.getTrackers()
        if (trackerListOfLocalDB.isNotEmpty()) {
            this.trackers = trackerListOfLocalDB
        } else {
            val result = getResult { exodusTrackerApi.getTrackerList() }
            if (result.isSuccess()) {
                result.data?.let {
                    trackerDao.saveTrackers(it.trackers.values.toList())
                }
            }
        }
    }

    private fun extractErrorMessage(appTrackerResult: Result<Map<String, TrackerInfo>>): String {
        return appTrackerResult.message ?: "Unknown Error"
    }

    private fun createAppTrackerListResult(
        appTrackerResult: Result<Map<String, TrackerInfo>>,
        appHandle: String
    ): Result<List<Tracker>> {
        appTrackerResult.data?.let {
            return Result.success(filterTrackersOfTheApp(it, appHandle))
        }
        return Result.error(extractErrorMessage(appTrackerResult))
    }

    private fun filterTrackersOfTheApp(
        appTrackerData: Map<String, TrackerInfo>,
        appHandle: String
    ): List<Tracker> {
        return trackers.filter {
            appTrackerData[appHandle]?.let { trackerInfo ->
                trackerInfo.reports[0].trackers.contains(it.id)
            } ?: false
        }
    }
}
