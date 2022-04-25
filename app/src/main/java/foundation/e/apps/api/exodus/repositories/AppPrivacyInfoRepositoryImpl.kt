package foundation.e.apps.api.exodus.repositories

import foundation.e.apps.api.Result
import foundation.e.apps.api.exodus.ExodusTrackerApi
import foundation.e.apps.api.exodus.Report
import foundation.e.apps.api.exodus.Tracker
import foundation.e.apps.api.exodus.TrackerDao
import foundation.e.apps.api.exodus.models.AppPrivacyInfo
import foundation.e.apps.api.getResult
import foundation.e.apps.utils.modules.CommonUtilsModule.LIST_OF_NULL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPrivacyInfoRepositoryImpl @Inject constructor(
    private val exodusTrackerApi: ExodusTrackerApi,
    private val trackerDao: TrackerDao
) : IAppPrivacyInfoRepository {
    private var trackers: List<Tracker> = listOf()

    override suspend fun getAppPrivacyInfo(appHandle: String): Result<AppPrivacyInfo> {
        val appTrackerInfoResult = getResult { exodusTrackerApi.getTrackerInfoOfApp(appHandle) }
        if (appTrackerInfoResult.isSuccess()) {
            return handleAppPrivacyInfoResultSuccess(appTrackerInfoResult)
        }
        return Result.error(extractErrorMessage(appTrackerInfoResult))
    }

    private suspend fun handleAppPrivacyInfoResultSuccess(
        appTrackerResult: Result<List<Report>>,
    ): Result<AppPrivacyInfo> {
        if (trackers.isEmpty()) {
            generateTrackerList()
        }
        return createAppPrivacyInfoResult(appTrackerResult)
    }

    private suspend fun generateTrackerList() {
        val trackerListOfLocalDB = trackerDao.getTrackers()
        if (trackerListOfLocalDB.isNotEmpty()) {
            this.trackers = trackerListOfLocalDB
        } else {
            generateTrackerListFromExodusApi()
        }
    }

    private suspend fun generateTrackerListFromExodusApi() {
        val result = getResult { exodusTrackerApi.getTrackerList() }
        if (result.isSuccess()) {
            result.data?.let {
                val trackerList = it.trackers.values.toList()
                trackerDao.saveTrackers(trackerList)
                this.trackers = trackerList
            }
        }
    }

    private fun extractErrorMessage(appTrackerResult: Result<List<Report>>): String {
        return appTrackerResult.message ?: "Unknown Error"
    }

    private fun createAppPrivacyInfoResult(
        appTrackerResult: Result<List<Report>>,
    ): Result<AppPrivacyInfo> {
        appTrackerResult.data?.let {
            return Result.success(getAppPrivacyInfo(it))
        }
        return Result.error(extractErrorMessage(appTrackerResult))
    }

    private fun getAppPrivacyInfo(
        appTrackerData: List<Report>,
    ): AppPrivacyInfo {
        /*
         * If the response is empty, that means there is no data on Exodus API about this app,
         * i.e. invalid data.
         * We signal this by list of "null".
         * It is not enough to send just empty lists, as an app can actually have zero trackers
         * and zero permissions. This is not to be confused with invalid data.
         *
         * Issue: https://gitlab.e.foundation/e/backlog/-/issues/5136
         */
        if (appTrackerData.isEmpty()) {
            return AppPrivacyInfo(LIST_OF_NULL, LIST_OF_NULL)
        }
        val sortedTrackerData =
            appTrackerData.sortedByDescending { trackerData -> trackerData.versionCode.toLong() }
        val appTrackers = extractAppTrackers(sortedTrackerData)
        val permissions = sortedTrackerData[0].permissions
        return AppPrivacyInfo(appTrackers, permissions)
    }

    private fun extractAppTrackers(sortedTrackerData: List<Report>): List<String> {
        return trackers.filter {
            sortedTrackerData[0].trackers.contains(it.id)
        }.map { it.name }
    }
}
