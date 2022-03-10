/*
 * Copyright (C) 2019-2022  E FOUNDATION
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import foundation.e.apps.R
import foundation.e.apps.manager.pkg.PackageInstallerService
import java.util.concurrent.TimeUnit

class UpdatesManager : BroadcastReceiver() {

    companion object {
        const val UPDATES_WORK_NAME = "updates_work"
        private const val TAG = "UpdatesManager"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive: ${intent.action}")
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val interval =
                preferences.getString(
                    context.getString(R.string.update_check_intervals),
                    context.getString(R.string.preference_update_interval_default)
                )!!.toLong()
            enqueueWork(context, interval, ExistingPeriodicWorkPolicy.KEEP)
        }
    }

    private fun buildWorkerConstraints() = Constraints.Builder().apply {
        setRequiresBatteryNotLow(true)
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    private fun buildPeriodicWorkRequest(interval: Long): PeriodicWorkRequest {
        return PeriodicWorkRequest.Builder(
            UpdatesWorker::class.java,
            interval,
            TimeUnit.HOURS
        ).apply {
            setConstraints(buildWorkerConstraints())
        }.build()
    }

    fun enqueueWork(context: Context, interval: Long, existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy) {
        Log.i(TAG, "UpdatesWorker interval: $interval hours")
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            UPDATES_WORK_NAME,
            existingPeriodicWorkPolicy, buildPeriodicWorkRequest(interval)
        )
        Log.i(TAG, "UpdatesWorker started")
    }
}
