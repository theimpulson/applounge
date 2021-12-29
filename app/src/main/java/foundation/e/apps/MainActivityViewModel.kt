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

package foundation.e.apps

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.aurora.gplayapi.helpers.AuthValidator
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.utils.DataStoreModule
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository,
    private val dataStoreModule: DataStoreModule,
    private val gson: Gson,
    private val notificationManager: NotificationManager,
    @Named("download") private val downloadNotificationChannel: NotificationChannel,
    @Named("update") private val updateNotificationChannel: NotificationChannel
) : ViewModel() {

    // Authentication Data for GPlay servers
    val authDataJson: LiveData<String> = dataStoreModule.authData.asLiveData()

    private var _authData: MutableLiveData<AuthData> = MutableLiveData()
    val authData: LiveData<AuthData> = _authData
    val authValidity: MutableLiveData<Boolean> = MutableLiveData()

    fun getAuthData() {
        viewModelScope.launch {
            fusedAPIRepository.fetchAuthData()
        }
    }

    fun destroyCredentials() {
        viewModelScope.launch {
            dataStoreModule.destroyCredentials()
        }
    }

    fun generateAuthData() {
        val data = gson.fromJson(authDataJson.value, AuthData::class.java)
        if (validateAuthData(data)) {
            _authData.value = data
            authValidity.value = true
        }
    }

    private fun validateAuthData(authData: AuthData): Boolean {
        return AuthValidator(authData).isValid()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        notificationManager.createNotificationChannel(downloadNotificationChannel)
        notificationManager.createNotificationChannel(updateNotificationChannel)
    }
}
