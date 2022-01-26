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

    override suspend fun getTrackerOfAnApp(appHandle: String): Result<List<Tracker>> {
        val appTrackerResult = getResult { exodusTrackerApi.getTrackerListOfAnApp(appHandle) }
        if (appTrackerResult.isSuccess()) {
            return getTrackerListOfTheApp(appTrackerResult, appHandle)
        }
        return Result.error(extractErrorMessage(appTrackerResult))
    }

    private suspend fun getTrackerListOfTheApp(
        appTrackerResult: Result<Map<String, TrackerInfo>>,
        appHandle: String
    ): Result<List<Tracker>> {
        return if (trackers.isNotEmpty()) {
            handleAppTrackerResult(appTrackerResult, appHandle)
        } else {
            getTrackerList()
            handleAppTrackerResult(appTrackerResult, appHandle)
        }
    }

    private suspend fun getTrackerList() {
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

    private fun handleAppTrackerResult(
        appTrackerResult: Result<Map<String, TrackerInfo>>,
        appHandle: String
    ): Result<List<Tracker>> {
        appTrackerResult.data?.let {
            return Result.success(findTrackersForApp(it, appHandle))
        }
        return Result.error(extractErrorMessage(appTrackerResult))
    }

    private fun findTrackersForApp(
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
