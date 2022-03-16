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

import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.database.fusedDownload.FusedDownload
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.Type
import foundation.e.apps.utils.modules.DataStoreModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.beryukhov.reactivenetwork.ReactiveNetwork
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val gson: Gson,
    private val dataStoreModule: DataStoreModule,
    private val fusedAPIRepository: FusedAPIRepository,
    private val fusedManagerRepository: FusedManagerRepository,
) : ViewModel() {

    val authDataJson: LiveData<String> = dataStoreModule.authData.asLiveData()
    val tocStatus: LiveData<Boolean> = dataStoreModule.tocStatus.asLiveData()
    val userType: LiveData<String> = dataStoreModule.userType.asLiveData()

    private var _authData: MutableLiveData<AuthData> = MutableLiveData()
    val authData: LiveData<AuthData> = _authData
    val authValidity: MutableLiveData<Boolean> = MutableLiveData()
    var authRequestRunning = false

    // Downloads
    val downloadList = fusedManagerRepository.getDownloadLiveList()
    var installInProgress = false
    private val _errorMessage = MutableLiveData<Exception>()
    val errorMessage: LiveData<Exception> = _errorMessage
    /*
     * Authentication related functions
     */

    fun getAuthData() {
        if (!authRequestRunning) {
            authRequestRunning = true
            viewModelScope.launch {
                fusedAPIRepository.fetchAuthData()
            }
        }
    }

    fun updateAuthData(authData: AuthData) {
        _authData.value = authData
    }

    fun destroyCredentials() {
        viewModelScope.launch {
            dataStoreModule.destroyCredentials()
        }
    }

    fun generateAuthData() {
        val data = gson.fromJson(authDataJson.value, AuthData::class.java)
        _authData.value = data
        viewModelScope.launch {
            authValidity.postValue(isAuthValid(data))
            authRequestRunning = false
        }
    }

    private suspend fun isAuthValid(authData: AuthData): Boolean {
        return fusedAPIRepository.validateAuthData(authData)
    }

    /*
     * Notification functions
     */

    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        fusedManagerRepository.createNotificationChannels()
    }

    /*
     * Download and cancellation functions
     */

    fun downloadApp(fusedDownload: FusedDownload) {
        viewModelScope.launch {
            fusedManagerRepository.downloadApp(fusedDownload)
        }
    }

    fun getApplication(app: FusedApp, imageView: ImageView?) {
        viewModelScope.launch {
            val fusedDownload: FusedDownload
            try {
                val appIcon = imageView?.let { getImageBase64(it) } ?: ""
                val downloadList: List<String>
                downloadList = getAppDownloadLink(app).toMutableList()

                fusedDownload = FusedDownload(
                    app._id,
                    app.origin,
                    app.status,
                    app.name,
                    app.package_name,
                    downloadList,
                    mutableMapOf(),
                    app.status,
                    app.type,
                    appIcon
                )
            } catch (e: Exception) {
                _errorMessage.value = e
                return@launch
            }

            if (fusedDownload.status == Status.INSTALLATION_ISSUE) {
                fusedManagerRepository.clearInstallationIssue(fusedDownload)
            }
            fusedManagerRepository.addDownload(fusedDownload)
        }
    }

    suspend fun updateAwaiting(fusedDownload: FusedDownload) {
        fusedManagerRepository.updateAwaiting(fusedDownload)
    }

    fun cancelDownload(app: FusedApp) {
        viewModelScope.launch {
            val fusedDownload =
                fusedManagerRepository.getFusedDownload(packageName = app.package_name)
            fusedManagerRepository.cancelDownload(fusedDownload)
        }
    }

    private suspend fun getAppDownloadLink(app: FusedApp): List<String> {
        val downloadList = mutableListOf<String>()
        authData.value?.let {
            if (app.type == Type.PWA) {
                downloadList.add(app.url)
            } else {
                downloadList.addAll(
                    fusedAPIRepository.getDownloadLink(
                        app._id,
                        app.package_name,
                        app.latest_version_code,
                        app.offer_type,
                        it,
                        app.origin
                    )
                )
            }
        }
        return downloadList
    }

    private fun getImageBase64(imageView: ImageView): String {
        val byteArrayOS = ByteArrayOutputStream()
        val bitmap = imageView.drawable.toBitmap()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
    }

    val internetConnection = liveData {
        emitSource(ReactiveNetwork().observeInternetConnectivity().asLiveData(Dispatchers.Default))
    }
}
