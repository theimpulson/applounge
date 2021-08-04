/*
 * Copyright (C) 2019-2021  E FOUNDATION
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import foundation.e.apps.R
import foundation.e.apps.updates.model.UpdatesWorker
import foundation.e.apps.utils.Constants
import java.util.concurrent.TimeUnit

class UpdatesManager : BroadcastReceiver() {
    private val TAG = "UpdatesManager"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val interval =
                preferences.getString(
                    context.getString(R.string.pref_update_interval_key),
                    context.getString(R.string.preference_update_interval_default)
                )!!
                    .toLong()
            enqueueWork(context, interval, ExistingPeriodicWorkPolicy.KEEP)
        }
    }

    private fun getWorkerConstraints() = Constraints.Builder().apply {
        setRequiresBatteryNotLow(true)
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    private fun getPeriodicWorkRequest(interval: Long): PeriodicWorkRequest {
        return PeriodicWorkRequest.Builder(
            UpdatesWorker::class.java,
            interval,
            TimeUnit.HOURS
        ).apply {
            setConstraints(getWorkerConstraints())
        }.build()
    }

    fun enqueueWork(context: Context, interval: Long, existingPeriodicWorkPolicy: ExistingPeriodicWorkPolicy) {
        Log.i(TAG, "UpdatesWorker interval: $interval hours")
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            Constants.UPDATES_WORK_NAME,
            existingPeriodicWorkPolicy, getPeriodicWorkRequest(interval)
        )
        Log.i(TAG, "UpdatesWorker started")
    }
}
