package foundation.e.apps.utils

import android.content.Context
import androidx.preference.PreferenceManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferenceManagerModule @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val preferenceManager = PreferenceManager.getDefaultSharedPreferences(context)

    fun preferredApplicationType(): String {
        val showFOSSApplications = preferenceManager.getBoolean("showFOSSApplications", false)
        val showPWAApplications = preferenceManager.getBoolean("showPWAApplications", false)

        return when {
            showFOSSApplications -> "open"
            showPWAApplications -> "pwa"
            else -> "any"
        }
    }
}