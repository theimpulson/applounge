/*
 * Apps  Quickly and easily install Android apps onto your device!
 * Copyright (C) 2021  E FOUNDATION
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.application

import androidx.lifecycle.LiveData
import androidx.lifecycle.LiveDataScope
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.Result
import foundation.e.apps.api.exodus.models.AppPrivacyInfo
import foundation.e.apps.api.exodus.repositories.IAppPrivacyInfoRepository
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.download.data.DownloadProgress
import foundation.e.apps.manager.download.data.DownloadProgressLD
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.round

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    downloadProgressLD: DownloadProgressLD,
    private val fusedAPIRepository: FusedAPIRepository,
    private val fusedManagerRepository: FusedManagerRepository,
    private val appPrivacyInfoRepository: IAppPrivacyInfoRepository
) : ViewModel() {

    val fusedApp: MutableLiveData<FusedApp> = MutableLiveData()
    val appStatus: MutableLiveData<Status?> = MutableLiveData()
    val downloadProgress = downloadProgressLD

    fun getApplicationDetails(id: String, packageName: String, authData: AuthData, origin: Origin) {
        viewModelScope.launch(Dispatchers.IO) {
            fusedApp.postValue(
                fusedAPIRepository.getApplicationDetails(
                    id,
                    packageName,
                    authData,
                    origin
                )
            )
        }
    }

    fun transformPermsToString(): String {
        var permissionString = ""
        fusedApp.value?.let {
            // Filter list to only keep platform permissions
            val filteredList = it.perms.filter {
                it.startsWith("android.permission.")
            }
            // Remove prefix as we only have platform permissions remaining
            val list = filteredList.map {
                it.replace("[^>]*permission\\.".toRegex(), "")
            }
            // Make it a dialog-friendly string and return it
            permissionString = list.joinToString(separator = "") { "$it<br />" }
        }
        return permissionString
    }

    fun handleRatingFormat(rating: Double): String {
        return if (rating % 1 == 0.0) {
            rating.toInt().toString()
        } else {
            rating.toString()
        }
    }

    fun fetchAppPrivacyInfo(): LiveData<Result<AppPrivacyInfo>> {
        return liveData {
            fusedApp.value?.let {
                if (it.trackers.isNotEmpty()) {
                    val appInfo = AppPrivacyInfo(it.trackers, it.perms)
                    emit(Result.success(appInfo))
                    return@liveData
                }
                val trackerResultOfAnApp = appPrivacyInfoRepository.getAppPrivacyInfo(it.package_name)
                handleAppTrackerResult(trackerResultOfAnApp, it)
            }
        }
    }

    private suspend fun LiveDataScope<Result<AppPrivacyInfo>>.handleAppTrackerResult(
        appPrivacyPrivacyInfoResult: Result<AppPrivacyInfo>,
        fusedApp: FusedApp
    ) {
        if (appPrivacyPrivacyInfoResult.isSuccess()) {
            handleAppPrivacyInfoSuccess(appPrivacyPrivacyInfoResult, fusedApp)
        } else {
            emit(Result.error("Tracker not found!"))
        }
    }

    private suspend fun LiveDataScope<Result<AppPrivacyInfo>>.handleAppPrivacyInfoSuccess(
        appPrivacyPrivacyInfoResult: Result<AppPrivacyInfo>,
        fusedApp: FusedApp
    ) {
        fusedApp.trackers = appPrivacyPrivacyInfoResult.data?.trackerList ?: listOf()
        if (fusedApp.perms.isEmpty()) {
            fusedApp.perms = appPrivacyPrivacyInfoResult.data?.permissionList ?: listOf()
        }
        emit(appPrivacyPrivacyInfoResult)
    }

    fun getTrackerListText(): String {
        fusedApp.value?.let {
            if (it.trackers.isNotEmpty()) {
                return it.trackers.joinToString(separator = "") { tracker -> "$tracker<br />" }
            }
        }
        return ""
    }

    fun getPrivacyScore(): Int {
        fusedApp.value?.let {
            return calculatePrivacyScore(it)
        }
        return -1
    }

    private fun calculatePrivacyScore(fusedApp: FusedApp): Int {
        return calculateTrackersScore(fusedApp.trackers.size) + calculatePermissionsScore(
            countAndroidPermissions(fusedApp)
        )
    }

    private fun countAndroidPermissions(fusedApp: FusedApp) =
        fusedApp.perms.filter { it.contains("android.permission") }.size

    private fun calculateTrackersScore(numberOfTrackers: Int): Int {
        return if (numberOfTrackers > 5) 0 else 9 - numberOfTrackers
    }

    private fun calculatePermissionsScore(numberOfPermission: Int): Int {
        return if (numberOfPermission > 9) 0 else round(0.2 * ceil((10 - numberOfPermission) / 2.0)).toInt()
    }

    suspend fun calculateProgress(progress: DownloadProgress): Pair<Long, Long> {
        fusedApp.value?.let { app ->
            val appDownload = fusedManagerRepository.getDownloadList().singleOrNull { it.id.contentEquals(app._id) }
            val downloadingMap = progress.totalSizeBytes.filter { item ->
                appDownload?.downloadIdMap?.keys?.contains(item.key) == true
            }
            val totalSizeBytes = downloadingMap.values.sum()
            val downloadedSoFar = progress.bytesDownloadedSoFar.filter { item -> appDownload?.downloadIdMap?.keys?.contains(item.key) == true }.values.sum()

            return Pair(totalSizeBytes, downloadedSoFar)
        }
        return Pair(1, 0)
    }
}
