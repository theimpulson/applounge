package foundation.e.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import foundation.e.apps.utils.PreferenceStorage

class MicrogUninstallListener : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        PreferenceStorage(context).save(context.getString(R.string.prefs_microg_vrsn_installed), false)
    }
}