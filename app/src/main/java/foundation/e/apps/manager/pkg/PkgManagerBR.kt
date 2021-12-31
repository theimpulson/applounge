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

package foundation.e.apps.manager.pkg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class PkgManagerBR : BroadcastReceiver() {

    private val TAG = PkgManagerBR::class.java.simpleName
    private val EXTRA_FAILED_UID = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_PACKAGE_ADDED) {
            val packageUid = intent.getIntExtra(Intent.EXTRA_UID, EXTRA_FAILED_UID)
            val packages = context.packageManager.getPackagesForUid(packageUid)
            packages?.let { Log.d(TAG, it[0].toString()) }
        }
    }
}
