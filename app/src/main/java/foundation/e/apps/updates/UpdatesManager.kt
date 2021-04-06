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

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import foundation.e.apps.R
import foundation.e.apps.updates.model.UpdatesWorker
import foundation.e.apps.utils.Constants
import java.util.concurrent.TimeUnit

class UpdatesManager(private val applicationContext: Context) {
    private val TAG = "UpdatesManager"
    private var automaticUpdateInterval: Int

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        automaticUpdateInterval =
                preferences.getString(
                        applicationContext.getString(R.string.pref_update_interval_key),
                        applicationContext.getString(R.string.preference_update_interval_default))!!
                        .toInt()
    }

    private fun getWorkerConstraints() = Constraints.Builder().apply {
        setRequiresBatteryNotLow(true)
        setRequiredNetworkType(NetworkType.CONNECTED)
    }.build()

    private fun getPeriodicWorkRequest() = PeriodicWorkRequest.Builder(
            UpdatesWorker::class.java,
            automaticUpdateInterval.toLong(),
            TimeUnit.HOURS).apply {
        setConstraints(getWorkerConstraints())
    }.build()

    fun startWorker() {
        Log.i(TAG, "UpdatesWorker interval: ${automaticUpdateInterval.toLong()} hours")
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(Constants.UPDATES_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, getPeriodicWorkRequest())
        Log.i(TAG, "UpdatesWorker started")
    }

    fun replaceWorker(automaticUpdateInterval: Int) {
        this.automaticUpdateInterval = automaticUpdateInterval
        Log.i(TAG, "UpdatesWorker interval: ${automaticUpdateInterval.toLong()} hours")
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(Constants.UPDATES_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, getPeriodicWorkRequest())
        Log.i(TAG, "UpdatesWorker started")
    }
}
