package foundation.e.apps.utils.pkg

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class PkgManagerBR : BroadcastReceiver() {

    private val TAG = PkgManagerBR::class.java.simpleName
    private val EXTRA_FAILED_UID = 0

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent?.action == Intent.ACTION_PACKAGE_ADDED) {
            val packageUid = intent.getIntExtra(Intent.EXTRA_UID, EXTRA_FAILED_UID)
            val packages = context.packageManager.getPackagesForUid(packageUid)
            packages?.let { Log.d(TAG, it.toString()) }
        }
    }
}
