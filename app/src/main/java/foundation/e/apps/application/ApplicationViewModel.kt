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

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aurora.gplayapi.data.models.AuthData
import dagger.hilt.android.lifecycle.HiltViewModel
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.database.fused.FusedDownload
import foundation.e.apps.manager.download.data.DownloadProgressLD
import foundation.e.apps.manager.fused.FusedManagerRepository
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import foundation.e.apps.utils.enums.Type
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import javax.inject.Inject

@HiltViewModel
class ApplicationViewModel @Inject constructor(
    private val fusedAPIRepository: FusedAPIRepository,
    private val downloadProgressLD: DownloadProgressLD,
    private val fusedManagerRepository: FusedManagerRepository
) : ViewModel() {

    val fusedApp: MutableLiveData<FusedApp> = MutableLiveData()
    val appStatus: MutableLiveData<Status?> = MutableLiveData()
    val downloadProgress = downloadProgressLD

    var fusedDownload: FusedDownload? = null

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

    fun getApplication(authData: AuthData, app: FusedApp, origin: Origin, imageView: ImageView) {
        viewModelScope.launch {
            val byteArrayOS = ByteArrayOutputStream()
            val bitmap = imageView.drawable.toBitmap()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOS)

            val downloadLink = if (app.type == Type.PWA) {
                app.url
            } else {
                fusedAPIRepository.getDownloadLink(
                    app._id,
                    app.package_name,
                    app.latest_version_code,
                    app.offer_type,
                    authData,
                    origin
                )
            }

            fusedDownload = FusedDownload(
                app._id,
                app.origin,
                app.status,
                app.name,
                app.package_name,
                downloadLink,
                0,
                app.status,
                app.type,
                Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT)
            )
            fusedDownload?.let { fusedManagerRepository.addDownload(it) }
        }
    }

    fun cancelDownload() {
        viewModelScope.launch {
            fusedDownload?.let { fusedManagerRepository.cancelDownload(it.downloadId) }
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

    fun handleRatingFormat(rating: Double): String {
        return if (rating % 1 == 0.0) {
            rating.toInt().toString()
        } else {
            rating.toString()
        }
    }
}
