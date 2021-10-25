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

package foundation.e.apps.settings

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.hilt.android.AndroidEntryPoint
import foundation.e.apps.BuildConfig
import foundation.e.apps.MainActivity
import foundation.e.apps.MainActivityViewModel
import foundation.e.apps.R

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val mainActivityViewModel: MainActivityViewModel by activityViewModels()

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        // Request application preference
        val requestApp = findPreference<Preference>("requestApplication")
        requestApp?.setOnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.appRequestFragment)
            true
        }

        // Refresh Session
        val refreshSession = findPreference<Preference>("refreshSession")
        refreshSession?.setOnPreferenceClickListener {
            mainActivityViewModel.destroyCredentials()
            Toast.makeText(view?.context, R.string.session_refreshed, Toast.LENGTH_SHORT).show()
            true
        }

        // About Apps
        val aboutApps = findPreference<Preference>("version")
        val cpuAbi = Build.SUPPORTED_ABIS[0]
        aboutApps?.summary = getString(
            R.string.apps_version_summary,
            BuildConfig.VERSION_NAME,
            BuildConfig.VERSION_CODE,
            cpuAbi
        )
        aboutApps?.setOnPreferenceClickListener {
            Toast.makeText(context, R.string.made_at_e, Toast.LENGTH_LONG).show()
            true
        }

        // Show applications preferences
        val showAllApplications = findPreference<RadioButtonPreference>("showAllApplications")
        val showFOSSApplications = findPreference<RadioButtonPreference>("showFOSSApplications")
        val showPWAApplications = findPreference<RadioButtonPreference>("showPWAApplications")

        showAllApplications?.setOnPreferenceChangeListener { _, _ ->
            showFOSSApplications?.isChecked = false
            showPWAApplications?.isChecked = false
            backToMainActivity()
            true
        }

        showFOSSApplications?.setOnPreferenceChangeListener { _, _ ->
            showAllApplications?.isChecked = false
            showPWAApplications?.isChecked = false
            backToMainActivity()
            true
        }

        showPWAApplications?.setOnPreferenceChangeListener { _, _ ->
            showFOSSApplications?.isChecked = false
            showAllApplications?.isChecked = false
            backToMainActivity()
            true
        }
    }

    private fun backToMainActivity() {
        Intent(context, MainActivity::class.java).also {
            activity?.finish()
            activity?.overridePendingTransition(0, 0)
            startActivity(it)
            activity?.overridePendingTransition(0, 0)
        }
    }
}
