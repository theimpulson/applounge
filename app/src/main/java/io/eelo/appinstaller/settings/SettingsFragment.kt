package io.eelo.appinstaller.settings

import android.os.Bundle
import android.support.v7.preference.CheckBoxPreference
import android.support.v7.preference.ListPreference
import android.support.v7.preference.PreferenceFragmentCompat
import io.eelo.appinstaller.R
import io.eelo.appinstaller.UpdatesManager

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
