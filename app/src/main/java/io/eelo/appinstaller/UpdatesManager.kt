package io.eelo.appinstaller

import android.content.Context
import android.preference.PreferenceManager
import android.util.Log
import androidx.work.*
import io.eelo.appinstaller.updates.model.UpdatesWorker
import io.eelo.appinstaller.utils.Constants
import java.util.concurrent.TimeUnit

class UpdatesManager(applicationContext: Context) {
    private val TAG = "UpdatesManager"
    private var automaticUpdateInterval: Int

    init {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        automaticUpdateInterval =
                preferences.getString(
                        applicationContext.getString(R.string.pref_update_interval_key),
                        applicationContext.getString(R.string.preference_update_interval_default))
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
        WorkManager.getInstance().enqueueUniquePeriodicWork(Constants.UPDATES_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, getPeriodicWorkRequest())
        Log.i(TAG, "UpdatesWorker started")
    }

    fun replaceWorker(automaticUpdateInterval: Int) {
        this.automaticUpdateInterval = automaticUpdateInterval
        Log.i(TAG, "UpdatesWorker interval: ${automaticUpdateInterval.toLong()} hours")
        WorkManager.getInstance().enqueueUniquePeriodicWork(Constants.UPDATES_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE, getPeriodicWorkRequest())
        Log.i(TAG, "UpdatesWorker started")
    }
}
