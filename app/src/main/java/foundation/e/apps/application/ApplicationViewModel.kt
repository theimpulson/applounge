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
import foundation.e.apps.api.exodus.Tracker
import foundation.e.apps.api.exodus.repositories.ITrackerRepository
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.download.data.DownloadProgressLD
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
    private val trackerRepository: ITrackerRepository
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

    fun transformPermsToString(permissions: MutableList<String>): String {
        // Filter list to only keep platform permissions
        val filteredList = permissions.filter {
            it.startsWith("android.permission.")
        }
        // Remove prefix as we only have platform permissions remaining
        val list = filteredList.map {
            it.replace("[^>]*permission\\.".toRegex(), "")
        }
        // Make it a dialog-friendly string and return it
        val permString = list.toString().replace(", ", "\n")
        return permString.substring(1, permString.length - 1)
    }

    fun handleRatingFormat(rating: Double): String {
        return if (rating % 1 == 0.0) {
            rating.toInt().toString()
        } else {
            rating.toString()
        }
    }

    fun fetchTrackerData(): LiveData<Result<List<String>>> {
        return liveData {
            fusedApp.value?.let {
                if (it.trackers.isNotEmpty()) {
                    emit(Result.success(it.trackers))
                    return@liveData
                }
                val trackerResultOfAnApp = trackerRepository.getTrackersOfAnApp(it.package_name)
                handleAppTrackerResult(trackerResultOfAnApp, it)
            }
        }
    }

    private suspend fun LiveDataScope<Result<List<String>>>.handleAppTrackerResult(
        trackerResultOfAnApp: Result<List<Tracker>>,
        fusedApp: FusedApp
    ) {
        if (trackerResultOfAnApp.isSuccess()) {
            val trackerList = trackerResultOfAnApp.data ?: listOf()
            fusedApp.trackers = trackerList.map { tracker -> tracker.name }
            emit(Result.success(fusedApp.trackers))
        } else {
            emit(Result.error("Tracker not found!", fusedApp.trackers))
        }
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
        return calculateTrackersScore(fusedApp.trackers.size) +
                calculatePermissionsScore(countAndroidPermissions(fusedApp))
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
