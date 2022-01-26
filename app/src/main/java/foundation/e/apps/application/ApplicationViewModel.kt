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

import android.util.Log
import androidx.lifecycle.*
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.exodus.Tracker
import foundation.e.apps.api.exodus.repositories.ITrackerRepository
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.api.fused.data.Origin
import foundation.e.apps.api.fused.data.Status
import foundation.e.apps.api.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository,
    private val trackerRepository: ITrackerRepository
) : ViewModel() {

    private val TAG = ApplicationViewModel::class.simpleName

    val fusedApp: MutableLiveData<FusedApp> = MutableLiveData()
    val appStatus: MutableLiveData<Status?> = MutableLiveData()

    private val _trackerListLiveData: MutableLiveData<Result<List<Tracker>>> = MutableLiveData()
    val trackerListLiveData: LiveData<Result<List<Tracker>>> = _trackerListLiveData

    private var trackerList: List<Tracker> = listOf()
    // Download Information
    private val appDownloadId: MutableLiveData<Long> = MutableLiveData()

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

    fun getApplication(authData: AuthData, app: FusedApp, origin: Origin) {
        appStatus.value = Status.DOWNLOADING
        viewModelScope.launch(Dispatchers.IO) {
            appDownloadId.postValue(
                fusedAPIRepository.getApplication(
                    app._id,
                    app.name,
                    app.package_name,
                    app.latest_version_code,
                    app.offer_type,
                    authData,
                    origin
                )
            )
        }
    }

    fun getYouTubeUrl(youTubeImg: String): String {
        val ytURL = "https://www.youtube.com/watch?v="
        val splitID = youTubeImg.split("https://i.ytimg.com/vi/")[1]
        val id = splitID.split("/")[0]
        return ytURL + id
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

    fun fetchTrackerData(): LiveData<Result<List<Tracker>>> {

        return liveData {
            fusedApp.value?.let {
                val trackerOfAnApp = trackerRepository.getTrackerOfAnApp(it.package_name)
                if(trackerOfAnApp.isSuccess()) {
                    trackerList = trackerOfAnApp.data ?: listOf()
                }
                emit(trackerOfAnApp)
            }
        }
    }

    fun getTrackerListText(): String {
        return trackerList.joinToString { tracker -> tracker.name + "\n" }
    }
}
