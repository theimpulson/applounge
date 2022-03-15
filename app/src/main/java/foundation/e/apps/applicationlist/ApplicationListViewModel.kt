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

package foundation.e.apps.applicationlist

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.utils.enums.Origin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApplicationListViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository
) : ViewModel() {

    val appListLiveData: MutableLiveData<List<FusedApp>> = MutableLiveData()

    fun getList(category: String, browseUrl: String, authData: AuthData, source: String) {
        if (appListLiveData.value?.isNotEmpty() == true) {
            Log.d("ApplicationListViewModel", "getList: ")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val packageNames = fusedAPIRepository.getAppsListBasedOnCategory(
                category,
                browseUrl,
                authData,
                source
            ).map { it.package_name }

            val applicationDetails = fusedAPIRepository.getApplicationDetails(
                packageNames, authData,
                getOrigin(source)
            )
            appListLiveData.postValue(applicationDetails)
        }
    }

    private fun getOrigin(source: String) =
        if (source.contentEquals("Open Source")) Origin.CLEANAPK else Origin.GPLAY
}
