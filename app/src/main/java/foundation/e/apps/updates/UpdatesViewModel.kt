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

package foundation.e.apps.updates

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.database.fused.FusedDownload
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.updates.manager.UpdatesManagerRepository
import foundation.e.apps.utils.enums.Type
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdatesViewModel @Inject constructor(
    private val updatesManagerRepository: UpdatesManagerRepository,
    private val fusedAPIRepository: FusedAPIRepository,
    private val fusedManagerRepository: FusedManagerRepository
) : ViewModel() {

    val updatesList: MutableLiveData<List<FusedApp>> = MutableLiveData()

    fun getUpdates(authData: AuthData) {
        viewModelScope.launch {
            updatesList.postValue(updatesManagerRepository.getUpdates(authData))
        }
    }

    fun getApplication(authData: AuthData, app: FusedApp) {
        viewModelScope.launch {
            val downloadLink = if (app.type == Type.PWA) {
                app.url
            } else {
                fusedAPIRepository.getDownloadLink(
                    app._id,
                    app.package_name,
                    app.latest_version_code,
                    app.offer_type,
                    authData,
                    app.origin
                )
            }
            val fusedDownload = FusedDownload(
                app._id,
                app.origin,
                app.status,
                app.name,
                app.package_name,
                downloadLink,
                0,
                app.status,
                app.type,
                app.icon_image_path
            )
            fusedManagerRepository.addDownload(fusedDownload)
        }
    }

    fun updateAllApps(authData: AuthData) {
        viewModelScope.launch {
            updatesList.value?.forEach { app ->
                getApplication(authData, app)
            }
        }
    }

    fun cancelDownload(packageName: String) {
        viewModelScope.launch {
            fusedManagerRepository.cancelDownload(packageName)
        }
    }
}
