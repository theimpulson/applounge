package foundation.e.apps.settings

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import foundation.e.apps.MainActivity
import foundation.e.apps.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)

        // Request application preference
        val requestApp = findPreference<Preference>("requestApplication")
        requestApp?.setOnPreferenceClickListener {
            view?.findNavController()?.navigate(R.id.appRequestFragment)
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
