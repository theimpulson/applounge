package foundation.e.apps

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.Result
import foundation.e.apps.api.exodus.models.AppPrivacyInfo
import foundation.e.apps.api.exodus.repositories.IAppPrivacyInfoRepository
import foundation.e.apps.api.fused.data.FusedApp
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.round

@HiltViewModel
class PrivacyInfoViewModel @Inject constructor(
    private val privacyInfoRepository: IAppPrivacyInfoRepository,
) : ViewModel() {

    fun getAppPrivacyInfoLiveData(fusedApp: FusedApp): LiveData<Result<AppPrivacyInfo>> {
        return liveData {
            emit(fetchEmitAppPrivacyInfo(fusedApp))
        }
    }

    private suspend fun fetchEmitAppPrivacyInfo(
        fusedApp: FusedApp
    ): Result<AppPrivacyInfo> {
        if (fusedApp.trackers.isNotEmpty() && fusedApp.perms.isNotEmpty()) {
            val appInfo = AppPrivacyInfo(fusedApp.trackers, fusedApp.perms)
            return Result.success(appInfo)
        }
        val appPrivacyPrivacyInfoResult =
            privacyInfoRepository.getAppPrivacyInfo(fusedApp.package_name)
        return handleAppPrivacyInfoResult(appPrivacyPrivacyInfoResult, fusedApp)
    }

    private fun handleAppPrivacyInfoResult(
        appPrivacyPrivacyInfoResult: Result<AppPrivacyInfo>,
        fusedApp: FusedApp
    ): Result<AppPrivacyInfo> {
        return if (appPrivacyPrivacyInfoResult.isSuccess()) {
            handleAppPrivacyInfoSuccess(appPrivacyPrivacyInfoResult, fusedApp)
        } else {
            Result.error("Tracker not found!")
        }
    }

    private fun handleAppPrivacyInfoSuccess(
        appPrivacyPrivacyInfoResult: Result<AppPrivacyInfo>,
        fusedApp: FusedApp
    ): Result<AppPrivacyInfo> {
        fusedApp.trackers = appPrivacyPrivacyInfoResult.data?.trackerList ?: listOf()
        if (fusedApp.perms.isEmpty()) {

            fusedApp.perms = appPrivacyPrivacyInfoResult.data?.permissionList ?: listOf()
        }
        return appPrivacyPrivacyInfoResult
    }

    fun getTrackerListText(fusedApp: FusedApp?): String {
        fusedApp?.let {
            if (it.trackers.isNotEmpty()) {
                return it.trackers.joinToString(separator = "") { tracker -> "$tracker<br />" }
            }
        }
        return ""
    }

    fun getPrivacyScore(fusedApp: FusedApp?): Int {
        fusedApp?.let {
            return calculatePrivacyScore(it)
        }
        return -1
    }

    fun calculatePrivacyScore(fusedApp: FusedApp): Int {
        val calculateTrackersScore = calculateTrackersScore(fusedApp.trackers.size)
        val calculatePermissionsScore = calculatePermissionsScore(
            countAndroidPermissions(fusedApp)
        )
        Log.d(
            "PrivacyInfoViewModel",
            "calculatePrivacyScore: ${fusedApp.name}: privacyScore: $calculateTrackersScore permissionScore: $calculatePermissionsScore noOfPermission: ${fusedApp.perms.size}"
        )
        return calculateTrackersScore + calculatePermissionsScore
    }

    private fun countAndroidPermissions(fusedApp: FusedApp) =
        fusedApp.perms.filter { it.contains("android.permission") }.size

    private fun calculateTrackersScore(numberOfTrackers: Int): Int {
        return if (numberOfTrackers > 5) 0 else 9 - numberOfTrackers
    }

    private fun calculatePermissionsScore(numberOfPermission: Int): Int {
        return if (numberOfPermission > 9) 0 else round(0.2 * ceil((10 - numberOfPermission) / 2.0)).toInt()
    }
}
