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

package foundation.e.apps.updates.manager

import com.aurora.gplayapi.data.models.AuthData
import foundation.e.apps.api.fused.FusedAPIRepository
import foundation.e.apps.api.fused.data.FusedApp
import foundation.e.apps.manager.pkg.PkgManagerModule
import foundation.e.apps.utils.enums.Origin
import foundation.e.apps.utils.enums.Status
import javax.inject.Inject

class UpdatesManagerImpl @Inject constructor(
    private val pkgManagerModule: PkgManagerModule,
    private val fusedAPIRepository: FusedAPIRepository
) {
    private val TAG = UpdatesManagerImpl::class.java.simpleName

    // TODO: MAKE THIS LOGIC MORE SANE
    suspend fun getUpdates(authData: AuthData): List<FusedApp> {
        val pkgList = mutableListOf<String>()
        val updateList = mutableListOf<FusedApp>()

        val userApplications = pkgManagerModule.getAllUserApps()
        userApplications.forEach { pkgList.add(it.packageName) }

        if (pkgList.isNotEmpty()) {
            // Get updates from CleanAPK
            val cleanAPKList = fusedAPIRepository.getApplicationDetails(
                pkgList,
                authData,
                Origin.CLEANAPK
            )
            cleanAPKList.first.forEach {
                if (it.package_name in pkgList) pkgList.remove(it.package_name)
                if (it.status == Status.UPDATABLE) updateList.add(it)
            }

            // Check for remaining apps from GPlay
            val gPlayList = fusedAPIRepository.getApplicationDetails(
                pkgList,
                authData,
                Origin.GPLAY
            )
            gPlayList.first.forEach {
                if (it.status == Status.UPDATABLE) updateList.add(it)
            }
        }
        return updateList
    }
}
