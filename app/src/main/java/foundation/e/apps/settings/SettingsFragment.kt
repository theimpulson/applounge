/*
    Copyright (C) 2019  e Foundation

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package foundation.e.apps.settings

import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import foundation.e.apps.R
import foundation.e.apps.updates.UpdatesManager

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Create preferences
        addPreferencesFromResource(R.xml.preferences)

        // Handle update check interval changes
        val updateCheckInterval =
                preferenceManager.findPreference(getString(R.string.pref_update_interval_key)) as ListPreference
        updateCheckInterval.setOnPreferenceChangeListener { _, newValue ->
            UpdatesManager(activity!!.applicationContext).replaceWorker(newValue.toString().toInt())
            true
        }

        // Disable auto update on WiFi preference if auto update is un-checked
        val automaticallyInstallUpdates =
                preferenceManager.findPreference(
                        getString(R.string.pref_update_install_automatically_key)) as CheckBoxPreference
        val onlyOnWifi =
                preferenceManager.findPreference(getString(R.string.pref_update_wifi_only_key)) as CheckBoxPreference
        onlyOnWifi.isEnabled = automaticallyInstallUpdates.isChecked
        automaticallyInstallUpdates.setOnPreferenceChangeListener { _, newValue ->
            onlyOnWifi.isEnabled = newValue.toString().toBoolean()
            true
        }
    }
}
