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

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import foundation.e.apps.manager.pkg.PkgManagerBR
import foundation.e.apps.manager.pkg.PkgManagerModule
import javax.inject.Inject

@HiltAndroidApp
class AppLoungeApplication : Application() {

    @Inject
    lateinit var pkgManagerModule: PkgManagerModule

    override fun onCreate() {
        super.onCreate()

        // Register broadcast receiver for package manager
        val pkgManagerBR = object : PkgManagerBR() {}
        registerReceiver(pkgManagerBR, pkgManagerModule.getFilter())
    }
}
