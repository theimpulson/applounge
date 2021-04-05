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

package foundation.e.apps.settings

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import foundation.e.apps.MainActivity
import foundation.e.apps.R
import foundation.e.apps.updates.UpdatesManager
import foundation.e.apps.utils.PreferenceStorage
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class SettingsFragment : PreferenceFragmentCompat() {
    private var oldCheckedPreference: RadioButtonPreference? = null


    @SuppressLint("RestrictedApi")
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Create preferences
        setPreferencesFromResource(R.xml.preferences, rootKey)


        val microGInstallState = preferenceManager.findPreference<Preference>(getString(R.string.prefs_microg_vrsn_installed))
        microGInstallState?.summary = if (context?.let { PreferenceStorage(it).getBoolean(getString(R.string.prefs_microg_vrsn_installed), false) }!!) {
            getString(R.string.microg_installed)
        } else {
            getString(R.string.microg_not_installed)
        }

        // Handle update check interval changes
        val updateCheckInterval =
                preferenceManager.findPreference<Preference>(getString(R.string.pref_update_interval_key)) as ListPreference
        updateCheckInterval.setOnPreferenceChangeListener { _, newValue ->
            UpdatesManager(activity!!.applicationContext).replaceWorker(newValue.toString().toInt())
            true
        }

        // Disable auto update on WiFi preference if auto update is un-checked
        val automaticallyInstallUpdates = preferenceManager.findPreference<Preference>(getString(R.string.pref_update_install_automatically_key)) as CheckBoxPreference

        val onlyOnWifi = preferenceManager.findPreference<Preference>(getString(R.string.pref_update_wifi_only_key)) as CheckBoxPreference
        onlyOnWifi.isEnabled = automaticallyInstallUpdates.isChecked
        automaticallyInstallUpdates.setOnPreferenceChangeListener { _, newValue ->
            onlyOnWifi.isEnabled = newValue.toString().toBoolean()
            true
        }

        // Launch AppRequestActivity when "Request app" preference is clicked
        val requestApp =
                preferenceManager.findPreference<Preference>(getString(R.string.pref_apps_request_app_key))
                        as Preference
        requestApp.setOnPreferenceClickListener { _ ->
            startActivity(Intent(context, AppRequestActivity::class.java))
            true
        }

        //Show all apps when checked
        var x = preferenceManager.findPreference<RadioButtonPreference>(getString(R.string.Show_all_apps)) as RadioButtonPreference
        //Show only open-source apps when checked
        var y = preferenceManager.findPreference<RadioButtonPreference>(getString(R.string.show_only_open_source_apps_key)) as RadioButtonPreference
        //Show only pwas when checked
        var z = preferenceManager.findPreference<RadioButtonPreference>(getString(R.string.show_only_pwa_apps_key)) as RadioButtonPreference

        x.setOnPreferenceChangeListener { _, newValue ->
            y.isChecked = false
            z.isChecked = false
            backToMainActivity()
            true
        }

        y.setOnPreferenceChangeListener { _, newValue ->
            x.isChecked = false
            z.isChecked = false
            backToMainActivity()
            true
        }

        z.setOnPreferenceChangeListener { _, newValue ->
            y.isChecked = false
            x.isChecked = false
            backToMainActivity()
            true
        }
    }


    private var working_dialog: ProgressDialog? = null

    fun backToMainActivity() {
        showWorkingDialog()
        val worker = Executors.newSingleThreadScheduledExecutor()
        val task = Runnable {
            run {
                removeWorkingDialog()
                val intent = Intent(activity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK
                Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                activity!!.finish()
            }
        }
        worker.schedule(task, 1, TimeUnit.SECONDS)
    }

    private fun showWorkingDialog() {
        working_dialog = ProgressDialog.show(context, "", "Applying Settings...", true)
    }

    private fun removeWorkingDialog() {
        if (working_dialog != null) {
            working_dialog!!.dismiss()
            working_dialog = null
        }
    }
}
