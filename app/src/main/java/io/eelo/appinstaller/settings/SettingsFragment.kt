package io.eelo.appinstaller.settings

import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import io.eelo.appinstaller.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create preferences
        addPreferencesFromResource(R.xml.preferences)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Empty
    }
}