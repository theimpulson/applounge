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

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.download.data.DownloadProgressLD
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    downloadProgressLD: DownloadProgressLD,
    private val fusedAPIRepository: FusedAPIRepository,
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
}
